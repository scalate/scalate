package org.fusesource.ssp.sample.resources

import com.sun.jersey.api.view.ImplicitProduces
import javax.ws.rs.{Produces, GET, Path, PathParam}

@ImplicitProduces(Array("text/html;qs=5"))
case class Item(id: String, name: String)

/**
 * A JAX-RS bean
 *
 * @version $Revision : 1.1 $
 */
@Path("/foo")
@ImplicitProduces(Array("text/html;qs=5"))
class FooResource {

  @GET
  @Produces(Array("application/xml", "text/xml"))
  def get = <hello>this is a foo!</hello>

  @Path("{itemid}/")
  def findITem(@PathParam("itemid") id: String) = {
    Item(id, "Name of Item: " + id)
  }

}