package org.fusesource.scalate.sample.resources

import com.sun.jersey.api.view.Viewable
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

  @GET
  @Path("sub")
  @Produces(Array("text/html;qs=5"))
  def scaml() = {
    new Viewable("sub", this);
  }

  @Path("{itemid}/")
  def findITem(@PathParam("itemid") id: String) = {
    Item(id, "Name of Item: " + id)
  }

}