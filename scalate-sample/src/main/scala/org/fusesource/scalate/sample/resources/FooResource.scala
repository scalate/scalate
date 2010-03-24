package org.fusesource.scalate.sample.resources

import _root_.com.sun.jersey.api.view.{ImplicitProduces, Viewable}
import javax.ws.rs.{Produces, GET, Path, PathParam}

/**
 * A JAX-RS bean
 *
 * @version $Revision : 1.1 $
 */
@Path("/foo")
class FooResource extends DefaultRepresentations {

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