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

package org.fusesource.scalate.tool

import collection.JavaConversions._
import java.net.{URLClassLoader, URL}
import java.{util => ju}
import java.io.{File, InputStream}
import java.util.Properties
import com.beust.jcommander._
import java.lang.{StringBuilder, String}
import shell.{Help, Shell}
import org.fusesource.jansi.AnsiConsole

/**
 * <p>
 * The extensible command line tool for Scalate.
 * </p>
 * <p>
 * Add new commands by:
 * <ol>
 *   <li>Implementing the the [[org.fusesource.scalate.tool.Command]] trait</li>
 *   <li>Add the class names of the new commands to a <code>META-INF/services/org.fusesource.scalate/commands</code> file in your jar</li>
 *   <li>Drop your new jar into the <code>$    { scalate.home } /lib</code> directory.</li>
 * </ol>
 * </p>
 *
 * @version $Revision : 1.1 $
 */
object Scalate {

  val homeDir = System.getProperty("scalate.home", "")
  lazy val scalateVersion = loadScalateVersion

  @Parameter(names=Array("--debug"), description="Enable debug logging")
  var debug_enabled = false

  def main(args: Array[String]) = {
    AnsiConsole.systemInstall
    try {
      intro
      val tool = new ScalateMain()
      val help = new Help();
      val jc = JCommander.newInstance(Array(tool, help, Scalate))
      jc.setProgramName(tool.getShellName)

      jc.parse(args:_*);
      if (help.help) {
        jc.usage()
      } else {
        tool.run(jc)
      }
    } catch {
      case e: ParameterException =>
        tool.displayFailure(null, e)
        println
        jc.usage
      case e =>
        tool.displayFailure(null, e)
    } finally {
      AnsiConsole.systemUninstall
    }
  }

  protected def loadScalateVersion(): Option[String] = {
    val pomProps = "META-INF/maven/org.fusesource.scalate/scalate-tool/pom.properties"
    try {
      val p = loadProperties(getClass.getClassLoader().getResourceAsStream(pomProps));
      if (p != null) {
        return Some(p.getProperty("version"));
      }
    } catch {
      case e: Exception =>
    }
    return None;
  }


  def loadProperties(is: InputStream): Properties = {
    if (is == null) {
      return null;
    }
    try {
      val p = new Properties()
      p.load(is);
      return p
    } catch {
      case e: Exception =>
        return null
    } finally {
      try {
        is.close()
      } catch {
        case _ =>
      }
    }
  }

  def intro() = {
    val is = scala.io.Source.fromInputStream(getClass.getResourceAsStream("banner.txt"))
    is.getLines().foreach(x=>info(x))
    info()
    if (scalateVersion == None) {
      info("Scalate Tool : http://scalate.fusesource.org/")
    } else {
      info("Scalate Tool v. " + scalateVersion.get + " : http://scalate.fusesource.org/")
    }
    info()
  }

  def debug(message: => String): Unit = {
    if (debug_enabled) {
      System.err.println("DEBUG: " + message)
    }
  }

  def info(message: => String = ""): Unit = {
    println(message)
  }

  def warn(message: => String): Unit = {
    System.err.println("WARN: " + message)
  }

  def error(message: => String): Unit = {
    System.err.println("ERROR: " + message)
  }

  def error(message: => String, exception: Throwable): Unit = {
    error(message)
    if (debug_enabled) {
      exception.printStackTrace
    }
  }
}

import Scalate._

class ScalateMain extends Shell {

  var _commands: List[CommandFactory] = null

  prompt = "\u001B[1m" + getShellName + ">\u001B[0m "

  override def getShellName() = "scalate"

  override def getDisplayedCommands() =  commands.map(_.name).toArray

  override def createSubCommand(name: String) = command(name).map(_.create).orNull

  override def usage(out: StringBuilder) = {
    def info(v: String) = {
      out.append(v + "\n");
    }
    super.usage(out);
    info("")
    info("For more help see http://scalate.fusesource.org/documentation/tool.html")
    info("")
  }


  override def run = {
    printStackTraces = debug_enabled
    super.run
  }

  def command(name: String) = {
    commands.filter(_.name == name).headOption
  }

  def commands: List[CommandFactory] = {
    if (_commands == null) {
      debug("loading commands")
      _commands = discoverCommands
    }
    _commands
  }


  def discoverCommands(): List[CommandFactory] = {
    val cl = extensionsClassLoader()
    Thread.currentThread.setContextClassLoader(cl)
    discoverCommandClasses().flatMap {
      name =>
        try {
          val clazz = cl.loadClass(name)
          try {
            Some(clazz.newInstance.asInstanceOf[CommandFactory])
          } catch {
            case e: Exception =>
              // It may be a scala object.. check for a module class
              try {
                val moduleField = cl.loadClass(name + "$").getDeclaredField("MODULE$")
                Some(moduleField.get(null).asInstanceOf[CommandFactory])
              } catch {
                case e2: Exception =>
                  // throw the original error...
                  throw e
              }
          }
        } catch {
          case e: Exception =>
            error("Invalid command class: " + name, e)
            None
        }
    }
  }

  def discoverCommandClasses(): List[String] = {
    var rc: List[String] = Nil
    val resources = extensionsClassLoader.getResources("META-INF/services/org.fusesource.scalate/commands.index")
    while (resources.hasMoreElements) {
      val url = resources.nextElement;
      debug("loaded commands from " + url)
      val p = loadProperties(url.openStream)
      if (p == null) {
        error("Could not load command list from: " + url)
      }
      val enum = p.keys
      while (enum.hasMoreElements) {
        rc = rc ::: enum.nextElement.asInstanceOf[String] :: Nil
      }
    }
    rc = rc.distinct
    debug("loaded commands: " + rc)
    return rc
  }

  def extensionsClassLoader(): ClassLoader = {
    if (homeDir.isEmpty) {
      getClass.getClassLoader
    } else {
      var classLoader = getClass.getClassLoader
      var urls: List[URL] = Nil

      val extensionDirs = List(new File(homeDir, "lib"))
      for (dir <- extensionDirs) {
        if (dir.isDirectory()) {
          val files = dir.listFiles();
          if (files != null) {
            // Sort the jars so that classpath built is consistently
            // in the same order. Also allows you to use jar
            // names to control classpath order.
            files.sortWith {
              (x, y) =>
                x.getName().compareTo(y.getName()) < 0
            }.foreach {
              file =>
                if (file.getName().endsWith(".zip") || file.getName().endsWith(".jar")) {
                  urls = urls ::: file.toURL() :: Nil
                }
            }
          }
        }
      }
      debug("extension classloader path: " + urls)
      new URLClassLoader(urls.toArray[URL], classLoader);
    }
  }

}