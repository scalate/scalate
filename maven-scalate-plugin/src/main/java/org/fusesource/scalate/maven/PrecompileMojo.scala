/**
 * Copyright (C) 2009-2010 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fusesource.scalate.maven

;

import java.io.File
import org.apache.maven.plugin.AbstractMojo
import org.fusesource.scalate.{TemplateSource, Binding, TemplateEngine};
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;



import org.fusesource.scalate.servlet.ServletRenderContext;
import org.fusesource.scalate.support.FileResourceLoader
import org.fusesource.scalate.util.IOUtil
import org.scala_tools.maven.mojo.annotations._


/**
 * This goal builds precompiles the Scalate templates
 * as Scala source files that be included in your standard
 * build. 
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@goal("precompile")
@phase("generate-sources")
@requiresProject
class PrecompileMojo extends AbstractMojo {

  @parameter
  @expression("${project}")
  @readOnly
  @required
  var project: MavenProject = _

  @parameter
  @description("The directory where the templates files are located.")
  @expression("${basedir}/src/main/webapp")
  var warSourceDirectory: File = _

  @parameter
  @description("The directory where resources are located.")
  @expression("${basedir}/src/main/resources")
  var resourcesSourceDirectory: File = _

  @parameter
  @description("The directory where the scala code will be generated into.")
  @expression("${project.build.directory}/generated-sources/scalate")
  var targetDirectory: File = _

  def execute() = {
    targetDirectory.mkdirs();

    getLog.debug("targetDirectory: " + targetDirectory)
    getLog.debug("warSourceDirectory: " + warSourceDirectory)
    getLog.debug("resourcesSourceDirectory: " + resourcesSourceDirectory)

    // TODO: need to customize bindings
    var engine = new TemplateEngine();
    engine.bindings = createBindings()

    engine.resourceLoader = new FileResourceLoader(Some(warSourceDirectory));

    val sourceDirs = List(warSourceDirectory, resourcesSourceDirectory)
    var paths = List[String]()
    for (extension <- engine.codeGenerators.keysIterator; sd <- sourceDirs if sd.exists) {
      paths = collectUrisWithExtension(sd, "", "." + extension) ::: paths;
    }

    getLog.info("Precompiling Scalate Templates into Scala classes...");

    for (uri <- paths) {
      // TODO it would be easier to just generate Source + URI pairs maybe rather than searching again for the source file???
      val file = sourceDirs.map(new File(_, uri)).find(_.exists).getOrElse(throw new Exception("Could not find " + uri + " in any paths " + sourceDirs))
      getLog.info("    processing " + file)

      val code = engine.generateScala(TemplateSource.fromFile(file, uri), createBindingsForPath(uri));
      val sourceFile = new File(targetDirectory, uri.replace(':', '_') + ".scala")
      sourceFile.getParentFile.mkdirs
      IOUtil.writeBinaryFile(sourceFile, code.source.getBytes("UTF-8"))
    }

    this.project.addCompileSourceRoot(targetDirectory.getCanonicalPath);
  }

  protected def collectUrisWithExtension(basedir: File, baseuri: String, extension: String): List[String] = {
    var collected = List[String]()
    if (basedir.isDirectory()) {
      var files = basedir.listFiles();
      if (files != null) {
        for (file <- files) {
          if (file.isDirectory()) {
            collected = collectUrisWithExtension(file, baseuri + "/" + file.getName(), extension) ::: collected;
          } else {
            if (file.getName().endsWith(extension)) {
              collected = baseuri + "/" + file.getName() :: collected;
            } else {
            }

          }
        }
      }
    }
    collected
  }


  protected def createBindings():List[Binding] = {
    List(Binding("context", classOf[ServletRenderContext].getName, true, isImplicit = true))
  }

  protected  def createBindingsForPath(uri:String): List[Binding] = {
    Nil
  }
}
