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
package org.fusesource.scalate.console

import _root_.java.io.File
import javax.servlet.ServletContext
import com.sun.jersey.api.representation.Form

import javax.ws.rs._
import org.fusesource.scalate.RenderContext
import org.fusesource.scalate.parsers.NoFormParameterException
import org.fusesource.scalate.rest.View
import org.fusesource.scalate.util.IOUtil
import slogging.StrictLogging

/**
 * @version $Revision : 1.1 $
 */
class ArchetypeResource(console: Console, name: String) extends ConsoleSnippets with StrictLogging {

  var _form: Form = _

  var src = System.getProperty("scalate.generate.src", "src")
  new File(src).mkdirs

  var srcMain = src + "/main"
  var srcMainScala = srcMain + "/scala"
  var srcMainJava = srcMain + "/java"
  var templatePrefix = "/WEB-INF/scalate/archetypes/"

  // START: ugly code that can be removed when Jersey supports nice injection in sub resources
  def renderContext = console.renderContext

  def servletContext: ServletContext = console.servletContext

  def request = console.request

  def response = console.response
  // END: ugly code that can be removed when Jersey supports nice injection in sub resources

  @Path("{name}")
  def child(@PathParam("name") childName: String) = new ArchetypeResource(console, name + "/" + childName)

  @GET
  @Produces(Array("text/html;qs=5"))
  def get = render(templatePrefix + name + ".index")

  @POST
  @Consumes(Array("application/x-www-form-urlencoded"))
  def post(form: Form) = {
    _form = form
    logger.debug("Posted: %s", form)

    // TODO - find the post template
    // validate it, if missing parameters, barf and re-render the view with the current values
    // and any validation errors added...

    val view = templatePrefix + name + ".post"
    render(view)
  }

  def form = _form

  def formParam(name: String): String = {
    val answer = _form.getFirst(name)
    if (answer == null) {
      throw new NoFormParameterException(name)
    } else {
      answer
    }
  }

  /**
   * Creates a file of the given name using the body as the content
   */
  def createFile(fileName: String)(body: => Unit): Unit = {
    logger.info("archetype creating file: %s", fileName)

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

  protected def render(view: String) = new View(view, Some(this))
}
