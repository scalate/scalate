package org.fusesource.scalate.tool

import java.util.{List => JList, Map => JMap}
import java.util.zip.ZipInputStream
import java.io.{FileInputStream, FileWriter, File, ByteArrayOutputStream}

/**
 * The command line tool for Scalate
 *
 * @version $Revision : 1.1 $
 */
object Scalate {
  val archetypes = Map("empty" -> "scalate-archetype-empty", "guice" -> "scalate-archetype-guice")
  var archetypeGroupId = "org.fusesource.scalate.tooling"
  val userDir = System.getProperty("user.dir", ".")
  val zipEntryPrefix = "archetype-resources/"
  var outputDir = userDir
  var packageName = ""
  var groupId = ""
  var artifactId = ""
  var version = "1.0-SNAPSHOT"
  var archetypeArtifactId = ""
  var name = ""
  var debug = false
  val homeDir = System.getProperty("scalate.home", "")


  def main(args: Array[String]): Unit = {
    println()
    println("Scalate Tool : http://scalate.fusesource.org/")
    println("Creates your Scalate project fast to get you scalate-ing!")
    println()

    if (homeDir.length == 0) {
      warn("scalate.home system property is not defined!")
    }
    debug("Scalate home dir = " + homeDir)

    if (args.length < 3) {
      println("Usage: archetype groupId artifactId [version] [packageName]")
      println()
      println("  archetype   : the archetype of project to create. Values are " + archetypeNames)
      println("  groupId     : the maven group Id of the new project")
      println("  artifactId  : the maven artifact Id of the new project")
      println("  version     : the version of the new project (defaults to 1.0-SNAPSHOT)")
      println("  packageName : the package name of generated scala code")
      println()
      println("For more help see http://scalate.fusesource.org/documentation/tool.html")
      println()
    }
    else {
      val archetypeName = args(0)
      val optArchetype = archetypes.get(archetypeName)
      if (optArchetype.isEmpty) {
        println("No such archetype '" + archetypeName + "' possible values are " + archetypeNames)
      }
      else {
        archetypeArtifactId = optArchetype.get
        groupId = args(1)
        artifactId = args(2)
        if (args.length > 3) {
          version = args(3)
        }
        if (args.length > 4) {
          packageName = args(4)
        }

        createArchetype()
      }
    }
  }

  def archetypeNames = archetypes.keysIterator.toSeq.sortWith(_ < _).mkString("(", ", ", ")")

  def createArchetype(): Unit = {

    // lets try find some files from the archetype...
    val archetypesDir = new File(homeDir + "/archetypes")
    val file = new File(archetypesDir, archetypeArtifactId + ".jar")
    if (!file.exists) {
      println("No such archetype '" + archetypeArtifactId + "' in directory " + archetypesDir)
    }
    else {
      outputDir = userDir + "/" + artifactId
      val outputFile = new File(outputDir)
      if (outputFile.exists) {
        println("Cannot create archetype as " + outputFile.getAbsolutePath + " already exists")
      }
      else {
        if (packageName.length == 0) {
          packageName = groupId + "." + artifactId
        }

        println("Creating archetype " + archetypeArtifactId + " using maven groupId: " +
                groupId + " artifactId: " + artifactId + " version: " + version
                + " in directory: " + outputDir)

        val zip = new ZipInputStream(new FileInputStream(file))
        try {
          var ok = true
          while (ok) {
            val entry = zip.getNextEntry
            if (entry == null) {
              ok = false
            }
            else {
              val fullName = entry.getName
              if (!entry.isDirectory && fullName.startsWith(zipEntryPrefix)) {
                name = fullName.substring(zipEntryPrefix.length)
                val longSize = entry.getSize
                val size = longSize.toInt
                debug("processing resource: " + name)
                val bos = new ByteArrayOutputStream()
                val buffer = new Array[Byte](64 * 1024)
                var bytes = 1
                while (bytes > 0) {
                  bytes = zip.read(buffer)
                  if (bytes > 0) {
                    bos.write(buffer, 0, bytes)
                  }
                }
                val text = new String(bos.toByteArray)
                processResource(text)
              }
              zip.closeEntry
            }
          }

          println()
          println("Done. To run the generated project type:")
          println()
          println("  cd " + artifactId)
          println("  mvn jetty:run")
          println()
          println("For more help see http://scalate.fusesource.org/documentation/")
          println()

        } finally {
          zip.close
        }
      }
    }
  }


  protected def processResource(fileContents: String): Unit = {
    val idx = name.lastIndexOf('/')
    val dirName = if (packageName.length > 0 && idx > 0 && shouldAppendPackage(name)) {
      outputDir + "/" + name.substring(0, idx) + "/" + packageName.replace('.', '/') + name.substring(idx)
    }
    else {
      outputDir + "/" + name
    }

    // lets replace properties...
    val dir = new File(dirName)
    dir.getParentFile.mkdirs
    val out = new FileWriter(dir)
    out.write(transformContents(fileContents))
    out.close
  }

  protected def transformContents(fileContents: String): String = {

    var answer = replaceVariable(fileContents, "package", packageName)

    if (name == "pom.xml") {
      // lets replace groupId and artifactId in pom.xml
      answer = answer.replaceFirst("""<groupId>.*</groupId>""", """<groupId>""" + groupId + """</groupId>""")
      answer = answer.replaceFirst("""<artifactId>.*</artifactId>""", """<artifactId>""" + artifactId + """</artifactId>""")
      answer = answer.replaceFirst("""<version>.*</version>""", """<version>""" + version + """</version>""")
    }
    answer
  }


  protected def replaceVariable(text: String, name: String, value: String): String = {
    text.replaceAll("""([^\\])\$\{""" + name + """\}""", "$1" + value)
  }

  protected def shouldAppendPackage(name: String) = name.matches("src/(main|test)/(java|scala)/.*")


  protected def debug(message: => String): Unit = {
    if (debug) {
      println("DEBUG: " + message)
    }
  }

  protected def warn(message: => String): Unit = {
    println("WARN: " + message)
  }

  protected def error(message: => String): Unit = {
    println("ERROR: " + message)
  }

  protected def error(message: => String, exception: Throwable): Unit = {
    error(message)
    exception.printStackTrace
  }
}