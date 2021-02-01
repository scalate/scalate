package org.fusesource.scalate.util

import slogging.LazyLogging

import java.io.InputStream
import java.util.Properties

/**
 * <p>
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object ClassFinder extends LazyLogging {

  def discoverCommands[T](
    indexPath: String,
    classLoaders: List[ClassLoader] = ClassLoaders.defaultClassLoaders): List[T] = {
    classLoaders.flatMap { cl =>
      ClassLoaders.withContextClassLoader(cl) {
        discoverCommandClasses(indexPath, cl).flatMap {
          name =>
            try {
              val clazz = cl.loadClass(name)
              try {
                Some(clazz.getConstructor().newInstance().asInstanceOf[T])
              } catch {
                case e: Exception =>
                  // It may be a scala object.. check for a module class
                  try {
                    val moduleField = cl.loadClass(name + "$").getDeclaredField("MODULE$")
                    Some(moduleField.get(null).asInstanceOf[T])
                  } catch {
                    case e2: Throwable =>
                      // throw the original error...
                      throw e
                  }
              }
            } catch {
              case e: Throwable =>
                logger.debug(s"Invalid class: $name", e)
                None
            }
        }
      }
    }.distinct
  }

  def discoverCommandClasses(
    indexPath: String,
    cl: ClassLoader = getClass.getClassLoader): List[String] = {
    var rc: List[String] = Nil
    val resources = cl.getResources(indexPath)
    while (resources.hasMoreElements) {
      val url = resources.nextElement
      logger.debug("loaded commands from %s", url)
      val p = loadProperties(url.openStream)
      if (p == null) {
        logger.warn("Could not load class list from: %s", url)
      }
      val enum = p.keys
      while (enum.hasMoreElements) {
        rc = rc ::: enum.nextElement.asInstanceOf[String] :: Nil
      }
    }
    rc = rc.distinct
    logger.debug("loaded classes: %s", rc)
    rc
  }

  def loadProperties(is: InputStream): Properties = {
    if (is == null) {
      null
    } else {
      try {
        val p = new Properties()
        p.load(is)
        p
      } catch {
        case e: Exception => null
      } finally {
        try {
          is.close()
        } catch {
          case _: Exception =>
        }
      }
    }
  }

}
