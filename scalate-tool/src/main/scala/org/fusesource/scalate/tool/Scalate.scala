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

import java.net.{URLClassLoader, URL}
import java.util.Properties
import java.io.{InputStream, FileInputStream, File}

/**
 * <p>
 * The extensible command line tool for Scalate.
 * </p>
 * <p>
 * Add new commands by:
 * <ol>
 *   <li>Implementing the the [[org.fusesource.scalate.tool.Command]] trait</li>
 *   <li>Add the class names of the new commands to a <code>META-INF/services/org.fusesoruce.scalate/commands</code> file in your jar</li>
 *   <li>Drop your new jar into the <code>${scalate.home}/lib</code> directory.</li>
 * </ol>
 * </p>
 *
 * @version $Revision : 1.1 $
 */
object Scalate {

  lazy val scalateVersion = loadScalateVersion
  val homeDir = System.getProperty("scalate.home", "")
  var debug_enabled = false

  var _commands: List[Command] = null

  def intro() = {
    info()
    if( scalateVersion == None ) {
      info("Scalate Tool : http://scalate.fusesource.org/")
    } else {
      info("Scalate Tool v. " + scalateVersion.get + " : http://scalate.fusesource.org/")
    }
    info()
  }
  
  def usage() = {
    intro()
    info("Usage: scalate [options] command [command-args]")
    info()
    info("Commands:")
    info()
    commands.foreach { command=>
      info(String.format("  %-12s : %s", command.name, command.summary))
    }
    info()
    info("Options:")
    info()
    info("  --debug     : Enables debug logging")
    info("  --help      : Shows this help screen")
    info()
    info("To get help for a command run:")
    info()
    info("  scalate command --help")
    info()
    info("For more help see http://scalate.fusesource.org/documentation/tool.html")
    info()
  }

  def loadScalateVersion():Option[String] = {
    val pomProps = "META-INF/maven/org.fusesource.scalate/scalate-tool/pom.properties"
    try {
        val p = loadProperties(getClass.getClassLoader().getResourceAsStream(pomProps));
        if (p != null) {
            return Some(p.getProperty("version"));
        }
    } catch {
      case e:Exception=>
    }
    return None;
  }

  def main(args: Array[String]): Unit = {
    if (homeDir.isEmpty) {
      warn("scalate.home system property is not defined!")
    }
    debug("Scalate home dir = " + homeDir)
    System.exit(process(args.toList))
  }

  def process(args: List[String]): Int = {

    args match {
      case next_arg :: the_rest =>
        next_arg match {
          case "--debug" =>
            this.debug_enabled = true
            process(the_rest)
          case "--help" | "-help" | "-?" =>
            if (the_rest.isEmpty) {
              usage()
            } else {
              command(the_rest.head) match {
                case Some(command) => command.usage
                case None => usage();
              }
            }
            return 0
          case _ =>
            command(next_arg) match {
              case Some(command) =>
                command.process(the_rest)
              case None =>
                info("Invalid syntax: unknown command: " + next_arg)
                usage()
                return -1;
            }
        }
      case Nil =>
        info("Invalid syntax: command not specified")
        usage()
        return -1;
    }
  }

  def command(name: String) = {
    commands.filter(_.name == name).headOption
  }

  def commands: List[Command] = {
    if (_commands == null) {
      debug("loading commands")
      _commands = discoverCommands
    }
    _commands
  }


  def discoverCommands(): List[Command] = {
    val cl = extensionsClassLoader()
    Thread.currentThread.setContextClassLoader(cl)
    discoverCommandClasses().flatMap {
      name =>
        try {
          Some(cl.loadClass(name).newInstance.asInstanceOf[Command])
        } catch {
          case e: Exception =>
            error("Invalid command class: " + name, e)
            None
        }
    }
  }

  def discoverCommandClasses(): List[String] = {
    var rc: List[String] = Nil
    val resources = extensionsClassLoader.getResources("META-INF/services/org.fusesoruce.scalate/commands.index")
    while(resources.hasMoreElements) {
      val url = resources.nextElement;
      debug("loaded commands from " + url)
      val p = loadProperties(url.openStream)
      if( p==null ) {
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

  def loadProperties(is:InputStream):Properties = {
    if( is==null ) {
      return null;
    }
    try {
      val p = new Properties()
      p.load(is);
      return p
    } catch {
      case e:Exception =>
      return null
    } finally {
      try {
        is.close()
      } catch {
        case _ =>
      }
    }
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
