package org.fusesource.scalate.rest

import com.sun.jersey.api.NotFoundException
import javax.ws.rs.{Path, PathParam}

/**
 * @version $Revision: 1.1 $
 */
abstract class ContainerResource[K,E,R] {
  def container: Container[K,E]

  @Path("id/{id}")
  def get(@PathParam("id") key : K) : R = {
    println("Loading id '" + key + "'")

    container.get(key) match {
      case Some(e) => createChild(e)
      case _ => throw new NotFoundException("Element " + key + " not found")
    }
  }

  def createChild(e: E) : R
}