package org.fusesource.scalate.samples.bookstore.resources

import com.sun.jersey.api.view.ImplicitProduces
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

/**
 * Defines the default representations to be used on resources
 *
 * @version $Revision: 1.1 $
 */
@ImplicitProduces(Array("text/html;qs=5"))
@Produces(Array(MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON))
trait DefaultRepresentations {

}