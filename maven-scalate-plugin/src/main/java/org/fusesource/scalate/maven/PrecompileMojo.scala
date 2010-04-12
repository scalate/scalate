/**
 * Copyright (C) 2009 Progress Software, Inc.
 * http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import org.fusesource.scalate.Binding;
import org.fusesource.scalate.TemplateEngine;
import org.fusesource.scalate.servlet.ServletRenderContext;
import org.fusesource.scalate.support.FileResourceLoader
import org.fusesource.scalate.util.IOUtil


/**
 * This goal builds precompiles the Scalate templates
 * as Scala source files that be included in your standard
 * build. 
 *
 * @goal precompile
 * @phase generate -sources
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class PrecompileMojo extends AbstractMojo {
  var project: MavenProject = null
  var warSourceDirectory: File = null
  var targetDirectory: File = null

  def execute() = {
    targetDirectory.mkdirs();

    // TODO: need to customize bindings
    var engine = new TemplateEngine();
    engine.bindings = createBindings()

    engine.resourceLoader = new FileResourceLoader(Some(warSourceDirectory));

    var paths = List[String]()
    for (extension <- engine.codeGenerators.keysIterator) {
      paths = collectUrisWithExtension(warSourceDirectory, "", "." + extension) ::: paths;
    }

    getLog().info("Precompiling Scalate Templates into Scala classes...");

    for (uri <- paths) {
      getLog().info("   processing: " + uri);
      val code = engine.generateScala(uri, createBindingsForPath(uri));
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
