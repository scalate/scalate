package org.fusesource.scalate.samples.scuery.resources

import com.sun.jersey.api.view.ImplicitProduces
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

/**
 * Defines the default representations to be used on resources
 *
 * @version $Revision: 1.1 $
 */
@ImplicitProduces(Array("text/html;qs=5"))
@Produces(Array("application/xml","text/xml", "application/json"))
trait DefaultRepresentations {

}