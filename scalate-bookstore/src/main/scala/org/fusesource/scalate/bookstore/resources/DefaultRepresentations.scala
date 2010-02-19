package org.fusesource.scalate.bookstore.resources

import com.sun.jersey.api.view.ImplicitProduces
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

/**
 * @version $Revision: 1.1 $
 */
@ImplicitProduces(Array("text/html;qs=5"))
@Produces(Array(MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON))
trait DefaultRepresentations {

}