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
package org.fusesource.scalate.rest

import java.io.OutputStream
import java.lang.annotation.Annotation
import java.lang.{ String, Class }

import javax.ws.rs.core.{ MultivaluedMap, MediaType }
import javax.ws.rs.ext.{ MessageBodyWriter, Provider }
import java.lang.reflect.Type

import scala.xml.NodeSeq

/**
 * Converts a Scala [[scala.xml.NodeSeq]] to a String for rendering nodes as HTML, XML, XHTML etc
 *
 * @version $Revision: 1.1 $
 */
@Provider
class NodeWriter extends MessageBodyWriter[NodeSeq] {

  def isWriteable(aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType) = {
    classOf[NodeSeq].isAssignableFrom(aClass)
  }

  def writeTo(nodes: NodeSeq, aClass: Class[_], aType: Type, annotations: Array[Annotation], mediaType: MediaType, stringObjectMultivaluedMap: MultivaluedMap[String, Object], outputStream: OutputStream): Unit = {
    val answer = nodes.toString();
    outputStream.write(answer.getBytes());
  }
}
