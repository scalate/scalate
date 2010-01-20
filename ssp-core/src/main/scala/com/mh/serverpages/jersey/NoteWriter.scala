package com.mh.serverpages.jersey

import java.io.OutputStream
import java.lang.annotation.Annotation
import java.lang.{String, Class}

import javax.ws.rs.core.{MultivaluedMap, MediaType}
import javax.ws.rs.ext.{MessageBodyWriter, Provider}
import java.lang.reflect.Type

import scala.xml.NodeSeq

/**
 * Converts a Scala {@link NodeSeq} to a String for rendering nodes as HTML, XML, XHTML etc
 *
 * @version $Revision: 1.1 $
 */
@Provider
class NodeWriter extends MessageBodyWriter[NodeSeq] {

  def isWriteable(aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = {
    classOf[NodeSeq].isAssignableFrom(aClass)
  }

  def getSize(nodes: NodeSeq, aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = -1L

  def writeTo(nodes: NodeSeq, aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType, stringObjectMultivaluedMap: MultivaluedMap[String, Object], outputStream: OutputStream) : Unit = {
    var answer = nodes.toString();
    outputStream.write(answer.getBytes());
  }
}
