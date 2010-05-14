package org.fusesource.scalate.rest

import java.io.OutputStream
import java.lang.reflect.Type
import java.net.{URL}
import javax.ws.rs.ext.{MessageBodyWriter, Provider}
import javax.servlet.ServletContext
import javax.ws.rs.core.{Context, MultivaluedMap, MediaType}

import org.fusesource.scalate.scuery.Transformer
import org.fusesource.scalate.ResourceNotFoundException
import org.fusesource.scalate.util.Logging

import com.sun.jersey.api.core.ExtendedUriInfo
import com.sun.jersey.api.container.ContainerException

import scala.collection.JavaConversions._
import xml.{XML, NodeSeq}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import java.lang.{String, Class}
import java.lang.annotation.Annotation
import org.fusesource.scalate.servlet.{ServletHelper, TemplateEngineServlet}

/**
 * Converts an Scuery  { @link Transformer } to output
 *
 * @version $Revision : 1.1 $
 */
@Provider
class TransformerWriter extends MessageBodyWriter[Transformer] with Logging {
  @Context
  protected var uriInfo: ExtendedUriInfo = _
  @Context
  protected var _servletContext: ServletContext = _
  @Context
  protected var request: HttpServletRequest = _
  @Context
  protected var response: HttpServletResponse = _

  protected var errorUris: List[String] = ServletHelper.errorUris()
  protected var templateDirectories = ServletHelper.templateDirectories


  def isWriteable(aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = {
    classOf[Transformer].isAssignableFrom(aClass)
  }

  def getSize(transformer: Transformer, aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = -1L


  def writeTo(transformer: Transformer, aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType, httpHeaders: MultivaluedMap[String, Object], out: OutputStream): Unit = {
    /*
        println("aClass: " + aClass.getName)
        println("transformer: " + transformer)
        println("uriInfo: " + uriInfo)
    */

    if (uriInfo != null) {
      var viewName = "index"
      val matchedTemplates = uriInfo.getMatchedTemplates()
      if (!matchedTemplates.isEmpty) {
        val lastTemplate = matchedTemplates.head
        val pathVariables = lastTemplate.getTemplateVariables()
        if (pathVariables.isEmpty) {
          val segments = lastTemplate.getTemplate.split("/").filter(_.length > 0)
          if (!segments.isEmpty) {
            viewName = segments.last
          }
        }
      }

      val resources = uriInfo.getMatchedResources
      if (!resources.isEmpty) {
        val resource = resources.head
        val className = resource.getClass.getName
        debug("resource class: " + className)
        debug("viewName: " + viewName)


        try {
          val templateName = "/" + className.replace('.', '/') + "." + viewName + ".html"
          var answer = render(templateName, transformer).toString
          // Ensure headers are committed
          out.flush()
          out.write(answer.getBytes)

        } catch {
          case e: Exception =>
            // lets forward to the error handler
            val servlet = TemplateEngineServlet()
            var notFound = true
            for (uri <- errorUris if notFound) {
              if (servletContext.getResource(uri) != null) {

                // we need to expose all the errors property here...
                request.setAttribute("javax.servlet.error.exception", e)
                request.setAttribute("javax.servlet.error.exception_type", e.getClass)
                request.setAttribute("javax.servlet.error.message", e.getMessage)
                request.setAttribute("javax.servlet.error.request_uri", request.getRequestURI)
                request.setAttribute("javax.servlet.error.servlet_name", request.getServerName)

                // TODO how to get the status code???
                val status = 500
                request.setAttribute("javax.servlet.error.status_code", status)

                request.setAttribute("it", e)
                servlet.render(uri, request, response)
                notFound = false
              }
            }
            if (notFound) {
              throw new ContainerException(e)
            }
        }
      }
      /*

            println("matchedURIs: " + uriInfo.getMatchedURIs)
            println("getPath: " + uriInfo.getPath)
            println("getPathSegments: " + uriInfo.getPathSegments().map(s => "" + s.getPath + " " + s.getMatrixParameters()))
            println("getMatchedResults: " + uriInfo.getMatchedResults())
            println("getMatchedTemplates: " + uriInfo.getMatchedTemplates())
            println("getPathParameters: " + uriInfo.getPathParameters)
      */
    }

  }

  /**
   * Renders the given template URI using the given ScQuery transformer
   */
  protected def render(template: String, transformer: Transformer): NodeSeq = {
    // lets load the template as XML...
    findResource(template) match {
      case Some(u) =>
        val xml = XML.load(u)
        // TODO report nice errors here if we can't parse it!!!
        transformer(xml)
      case _ => throw new ResourceNotFoundException(template)
    }
  }

  protected def findResource(path: String): Option[URL] = {
    var answer: Option[URL] = None
    val paths = for (dir <- templateDirectories if answer.isEmpty) {
      val t = dir + path
      debug("Trying to find template: " + t)
      val u = servletContext.getResource(t)
      debug("Found: " + u)
      if (u != null) {
        answer = Some(u)
      }
    }
    answer
  }


  /**
   * Returns the servlet context injected by JAXRS
   */
  protected def servletContext: ServletContext = {
    if (_servletContext == null) {
      throw new IllegalArgumentException("servletContext not injected")
    }
    _servletContext
  }

}
