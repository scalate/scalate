/**
 * Copyright (C) 2009-2011 the original author or authors.
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
package org.fusesource.scalate.tool.commands

import java.{util => ju, lang => jl}
import java.util.zip.ZipInputStream
import java.io._
import java.lang.StringBuilder
import org.apache.felix.gogo.commands.{Action, Option => option, Argument => argument, Command => command, CompleterValues => completerValues}
import org.apache.felix.service.command.CommandSession
import java.util.regex.Pattern
import org.fusesource.scalate.util.IOUtil._


/**
 * The 'scalate create' sub command.
 */
@command(scope = "scalate", name = "create", description = "Creates your Scalate project fast to get you scalate-ing!")
class Create extends Action {

  @argument(index = 0, required = true, name = "archetype", description = "Archetype to create")
  // TODO rename to archetype
  var archetype: String = _
  @argument(index = 1, required = true, name = "groupId", description = "Maven group Id of the new project")
  var groupId = ""
  @argument(index = 2, required = true, name = "artifactId", description = "Maven artifact Id of the new project")
  var artifactId: String = _
  @argument(index = 3, name = "version", description = "Maven Version of the new project")
  var version = "1.0-SNAPSHOT"
  @argument(index = 4, name = "package", description = "Package name of generated scala code (defaults to 'groupId.artifactId')")
  var packageName: String = _

  @option(name = "--outputDir", description = "Output directory")
  var outputDir = userDir
  @option(name = "--verbose", description = "Verbose output")
  var verbose: Boolean = false
  @option(name = "--home", description = "Scalate install directory")
  var homeDir = System.getProperty("scalate.home", "")
  
  val archetypes = Map(
    "jersey" -> "scalate-archetype-jersey_2.9",
    "guice" -> "scalate-archetype-guice_2.9",
    "sitegen" -> "scalate-archetype-sitegen_2.9"
  )

  var archetypeGroupId = "org.fusesource.scalate.tooling"
  val userDir = System.getProperty("user.dir", ".")
  val zipEntryPrefix = "archetype-resources/"
  var archetypeArtifactId = ""
  var name = ""

  val binarySuffixes = List(".png", ".ico", ".gif", ".jpg", ".jpeg", ".bmp")

/*
  TODO

  def usage(out: StringBuilder) = {
    def info(v: String) = {
      out.append(v + "\n");
    }
    info("")
    info("  Archetypes:")
    info("    empty  Basic Scalate project")
    info("    guice  Guice based Scalate project")
  }
*/

  @completerValues(index = 0)
  def archetypeNameArray = archetypes.keysIterator.toSeq.sortWith(_ < _).toArray

  def archetypeNames = archetypes.keysIterator.toSeq.sortWith(_ < _).mkString("(", ", ", ")")

  protected val webInfResources = "src/main/webapp/WEB-INF/resources"
  protected val sourcePathRegexPattern = "(src/(main|test)/(java|scala)/)(.*)".r.pattern

  def execute(session: CommandSession): jl.Integer = {
    def info(s: => String = "") = session.getConsole.println(s)
    def debug(s: => String) = if (verbose) session.getConsole.println(s)

    val optArchetype = archetypes.get(archetype)
    if (optArchetype.isEmpty) {
      info("No such archetype '" + archetype + "' possible values are " + archetypeNames)
      return -1
    }
    this.archetypeArtifactId = optArchetype.get

    // lets try find some files from the archetype...
    val archetypesDir = new File(homeDir + "/archetypes")
    val file = new File(archetypesDir, archetypeArtifactId + ".jar")
    if (!file.exists) {
      info("No such archetype '" + archetypeArtifactId + "' in directory " + archetypesDir)
      return -2
    }
    else {
      outputDir = userDir + "/" + artifactId
      val outputFile = new File(outputDir)
      if (outputFile.exists) {
        info("Cannot create archetype as " + outputFile.getAbsolutePath + " already exists")
        return -2
      } else {
        if (packageName==null || packageName.length == 0) {
          packageName = groupId + "." + artifactId
        }

        info("Creating archetype " + archetypeArtifactId + " using maven groupId: " +
                groupId + " artifactId: " + artifactId + " version: " + version
                + " in directory: " + outputDir)

        using(new ZipInputStream(new FileInputStream(file))) { zip=>
          var ok = true
          while (ok) {
            val entry = zip.getNextEntry
            if (entry == null) {
              ok = false
            } else {
              val fullName = entry.getName
              if (!entry.isDirectory && fullName.startsWith(zipEntryPrefix)) {
                name = fullName.substring(zipEntryPrefix.length)
                val longSize = entry.getSize
                val size = longSize.toInt
                debug("processing resource: " + name)

                val idx = name.lastIndexOf('/')
                val matcher = sourcePathRegexPattern.matcher(name)
                val dirName = if (packageName.length > 0 && idx > 0 && matcher.matches) {
                  val prefix = matcher.group(1)
                  outputDir + "/" + prefix + packageName.replace('.', '/') + "/" + name.substring(prefix.length)
                } else if (packageName.length > 0 && name.startsWith(webInfResources)) {
                  outputDir + "/src/main/webapp/WEB-INF/" + packageName.replace('.', '/') + "/resources" + name.substring(webInfResources.length)
                } else {
                  outputDir + "/" + name
                }

                // lets replace properties...
                val file = new File(dirName)
                file.getParentFile.mkdirs
                using(new FileOutputStream(file)) { out =>
                  if( binarySuffixes.find( name.endsWith(_) ).isDefined ) {
                    // binary file?  don't transform.
                    copy(zip, out)
                  } else {
                    // text file...
                    val bos = new ByteArrayOutputStream()
                    copy(zip, bos)
                    val text = new String(bos.toByteArray, "UTF-8")
                    out.write(transformContents(text).getBytes())
                  }
                }
              }
              zip.closeEntry
            }
          }

          info()
          info("Done. To run the generated project type:")
          info()
          info("  cd " + artifactId)
          info("  mvn jetty:run")
          info()

        }
        return 0
      }
    }
  }

  protected def transformContents(fileContents: String): String = {
    var answer = replaceVariable(fileContents, "package", packageName)
    answer = replaceVariable(answer, "groupId", groupId)
    answer = replaceVariable(answer, "artifactId", artifactId)
    answer = replaceVariable(answer, "version", version)
    answer
  }

  protected def replaceVariable(text: String, name: String, value: String): String = {
    text.replaceAll(Pattern.quote("${"+name+"}"), value)
  }

}