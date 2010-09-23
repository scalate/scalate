package org.fusesource.scalate.support

import org.fusesource.scalate.TemplateEngine
import util.matching.Regex

/**
 * A helper object to find a template from a URI using a number of possible extensions and directories
 */
class TemplateFinder(engine: TemplateEngine) {

  var hiddenPatterns = List("""^_|/_""".r, """^\.|/\.""".r)
  var replacedExtensions = List(".html", ".htm")
  lazy val extensions = engine.extensions

  def findTemplate(rootOrPath: String): Option[String] = {
    val path = if (rootOrPath == "/") "/index" else rootOrPath

    for( p <- hiddenPatterns ; if p.findFirstIn(path).isDefined ) {
      return None
    }

    // Is the uri a direct path to a template??
    // i.e: /path/page.jade -> /path/page.jade
    def findDirect(uri: String): Option[String] = {
      for (base <- engine.templateDirectories; ext <- extensions) {
        val path = base + uri
        if (path.endsWith(ext) && engine.resourceLoader.exists(path)) {
          return Some(path)
        }
      }
      return None
    }

    // Lets try to find the template by appending a template extension to the path
    // i.e: /path/page.html -> /path/page.html.jade
    def findAppended(uri: String): Option[String] = {
      for (base <- engine.templateDirectories; ext <- extensions) {
        val path = base + uri + "." + ext
        if (engine.resourceLoader.exists(path)) {
          return Some(path)
        }
      }
      return None
    }

    // Lets try to find the template by replacing the extension
    // i.e: /path/page.html -> /path/page.jade
    def findReplaced(): Option[String] = {
      replacedExtensions.foreach {ext =>
        if (path.endsWith(ext)) {
          val rc = findAppended(path.stripSuffix(ext))
          if (rc != None)
            return rc
        }
      }
      None
    }

    findDirect(path).orElse(findAppended(path).orElse(findReplaced()))
  }
}