package org.fusesource.scalate.console

import _root_.java.io.File
import javax.servlet.ServletContext
import com.sun.jersey.api.representation.Form
import com.sun.jersey.api.view.Viewable
import javax.ws.rs._
import org.fusesource.scalate.util.{Logging, IOUtil}
import org.fusesource.scalate.{NoFormParameterException, RenderContext}

/**
 * @version $Revision : 1.1 $
 */
class ArchetypeResource(console: Console, name: String) extends ConsoleSnippets with Logging {
  var _form: Form = _

  var src = System.getProperty("scalate.generate.src", "src")
  new File(src).mkdirs

  var srcMain = src + "/main"
  var srcMainScala = srcMain + "/scala"
  var srcMainJava = srcMain + "/java"
  var templatePrefix = "/WEB-INF/scalate/archetypes/"

  def renderContext = console.renderContext
  def servletContext: ServletContext = console.servletContext

  @Path("{name}")
  def child(@PathParam("name") childName: String) = new ArchetypeResource(console, name + "/" + childName)

  @GET
  @Produces(Array("text/html;qs=5"))
  def get = {
    val view = templatePrefix + name + ".index"
    new Viewable(view, this)
  }

  @POST
  @Consumes(Array("application/x-www-form-urlencoded"))
  def post(form: Form) = {
    _form = form
    debug("Posted: " + form)

    // TODO - find the post template
    // validate it, if missing parameters, barf and re-render the view with the current values
    // and any validation errors added...

    val view = templatePrefix + name + ".post"
    new Viewable(view, this)
  }


  def form = _form

  def formParam(name: String): String = {
    val answer = _form.getFirst(name)
    if (answer == null) {
      throw new NoFormParameterException(name)
    }
    else {
      answer
    }
  }

  /**
   * Creates a file of the given name using the body as the content
   */
  def createFile(fileName: String)(body: => Unit): Unit = {
    info("archetype creating file:" + fileName)
    
    val text = RenderContext.capture(body)

    // lets make the parent directories
    IOUtil.makeParentDirs(fileName)

    IOUtil.writeText(fileName, text)
  }

  /**
   * Returns the scala source file name for the given class name
   */
  def scalaSourceFileName(className: String): String = srcMainScala + "/" + className.replace('.', '/') + ".scala"

  /**
   * Returns the java source file name for the given class name
   */
  def javaSourceFileName(className: String): String = srcMainJava + "/" + className.replace('.', '/') + ".java"

}