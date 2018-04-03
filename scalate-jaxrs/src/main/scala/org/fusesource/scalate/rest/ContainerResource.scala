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

import javax.ws.rs.{ POST, Path, PathParam }
import org.fusesource.scalate.util.{ Log }
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status

object ContainerResource extends Log

/**
 * @version $Revision: 1.1 $
 */
abstract class ContainerResource[K, E, R] {
  import ContainerResource._

  def container: Container[K, E]

  @Path("id/{id}")
  def get(@PathParam("id") key: K): R = {
    debug("Loading id '%s'", key)

    container.get(key) match {
      case Some(e) => createChild(e)
      case _ => throw createNotFoundException("Element " + key + " not found")
    }
  }

  @POST
  def post(element: E) = {
    // TODO validate the new element
    container.put(element)
  }

  def createChild(e: E): R

  def createNotFoundException(message: String) = {
    new WebApplicationException(Response.status(Status.NOT_FOUND).entity(message).`type`("text/plain").build())
  }
}
