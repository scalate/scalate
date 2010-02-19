package org.fusesource.scalate.rest

import javax.ws.rs.{PUT, DELETE}

/**
 * @version $Revision: 1.1 $
 */
class ElementResource[K,E](element: E, container: Container[K,E]) {

  @PUT
  def put(updatedElement: E) : Unit = {
    // TODO validate the new element
    container.put(updatedElement)
  }

  @DELETE
  def delete: Unit = {
    container.remove(element)
  }
}