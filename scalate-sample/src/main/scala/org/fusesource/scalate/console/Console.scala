package org.fusesource.scalate.console

import javax.ws.rs.Path

/**
 * The Scalate development console
 *
 * @version $Revision: 1.1 $
 */
@Path("/scalate")
class Console extends DefaultRepresentations {

  @Path("menu")
  def menu = new Menu(this)
  
}