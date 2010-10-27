package org.fusesource.scalate.wikitext

import collection.mutable.HashMap
import java.net.URI
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType
import org.eclipse.mylyn.internal.wikitext.confluence.core.block.AbstractConfluenceDelimitedBlock
import io.Source
import compat.Platform
import scala.Option
import java.io.{FileInputStream, File, InputStream}
import org.eclipse.mylyn.wikitext.core.parser.{DocumentBuilder, Attributes}
import org.fusesource.scalate.util.{Logging, IOUtil}

/**
 * Helper class to access file containing snippets of code:
 * - on the local file system
 * - using a full URL
 * - using a URL that starts with a predefined prefix
 */
object Snippets extends Logging {

  var errorHandler: (SnippetBlock, Throwable) => Unit = logError

  var failOnError = false

  val prefixes = HashMap[String, String]()

  /**
   * Add a prefix definition to make it easier to access snippets.
   * E.g. if you add 'activemq' -> 'http://svn.apache.org/repos/asf/activemq/trunk'
   *      you can use 'activemq/pom.xml' as a shorthand notation for 'http://svn.apache.org/repos/asf/activemq/trunk/pom.xml"
   *
   *
   * @param prefix the prefix
   * @param location the full URL that should be used for the prefix
   */
  def addPrefix(prefix: String, location: String) : Unit = prefixes.put(prefix, location)

  private def handlePrefix(location: String) : String = {
    val prefix = location.substring(0, location.indexOf("/"))
    if (prefixes.contains(prefix)) {
      location.replace(prefix, prefixes(prefix))
    } else {
      location
    }
  }

  /**
   * Get the snippet file contents
   */
  def getSource(location: String) : Source = {
    val uri = new URI(handlePrefix(location))
    Source.fromInputStream(if (uri.isAbsolute) {
      uri.toURL.openStream
    } else {
      // if the URI is not absolute, let's try using it as relative file path
      new FileInputStream(new File(location))
    }, "UTF-8")    
  }

  protected def logError(snippet: SnippetBlock, e: Throwable): Unit = {
    error("Failed to generate snippet: " + snippet.url + ". " + e, e)
    if (failOnError) {
      throw e
    }
  }
}

/**
 * Represents a {snippet} block in the wiki markup
 */
class SnippetBlock extends AbstractConfluenceDelimitedBlock("snippet") with Logging {

  var lang:Option[String] = None
  var url: String = _
  var id:Option[String] = None
  var pygmentize : Boolean = false

  lazy val handler : SnippetHandler = {
    if (pygmentize) {
      val block = new PygementsBlock
      block.setState(state)
      block.setParser(parser)
      block.setOption("lang", language)
      PygmentizeSnippetHandler(block)
    } else {
      DefaultSnippetHandler(builder, language)
    }
  }

  override def beginBlock() = handler.begin

  override def handleBlockContent(value:String) = {
    // graciously do nothing 
  }

  override def endBlock() = {
    try {
      for (line <- getSnippet) {
        handler.addLine(line)
      }
    }
    catch {
      case e =>
        Snippets.errorHandler(this, e)
    }
    handler.done
  }

  /**
   * Extract the snippet from the Source file
   */
  def getSnippet : Iterator[String] = {
    val lines = Snippets.getSource(url).getLines
    id match {
      case None => lines
      case Some(snippet) => lines.dropWhile(!_.contains("START SNIPPET: " + snippet))
                                 .takeWhile(!_.contains("END SNIPPET: " + snippet))
                                 .drop(1)
    }
  }

  override def setOption(key: String, value:String) = {
    key match {
      case "id" => id = Some(value)
      case "url" => url = value
      case "lang" => lang = Some(value)
      case "pygmentize" => pygmentize = value.toBoolean
      case n => warn("Ignored snippet attribute " + n + " on " + this)
    }
  }

  /**
   * Get the configured language or determine the language from the file extension
   */
  def language = {
    lang match {
      case None => extension(url)
      case Some(lang) => lang
    }
  }

  /**
   * Extract the file extension from the URL
   */
  def extension(url: String) = {
    if (url.contains(".")) {
      url.split('.').last
    } else {
      ""
    }
  }

  override def toString = "{snippet:url=" + url + "}"
}

/**
 * Trait to define a {snippet} handler
 */
trait SnippetHandler {

  def begin
  def addLine(line: String)
  def done

}

/**
 * Default handler for the {snippet} code (renders a <div class="snippet"><pre class="<language>">
 */
case class DefaultSnippetHandler(val builder: DocumentBuilder, val language: String) extends SnippetHandler {

  def begin = {
    builder.beginBlock(BlockType.DIV, cssClass("snippet"))
    builder.beginBlock(BlockType.PREFORMATTED, cssClass(language));
    builder.characters("\n")
  }

  def addLine(line: String) = {
    builder.characters(line + "\n")
  }

  def done = {
    builder.endBlock();  // </pre>
    builder.endBlock();  // </div>          
  }

  /**
   * Create attributes instance containing the CSS class
   */
  def cssClass(cssClass: String) = {
    val attributes = new Attributes
    attributes.setCssClass(cssClass)
    attributes
  }
  
}

/**
 * Uses pygmentize to handles syntax coloring for the {snippet}'s code
 */
case class PygmentizeSnippetHandler(val block: PygementsBlock) extends SnippetHandler {

  def begin = block.beginBlock
  def addLine(line: String) = block.handleBlockContent(line)
  def done = block.endBlock

}
