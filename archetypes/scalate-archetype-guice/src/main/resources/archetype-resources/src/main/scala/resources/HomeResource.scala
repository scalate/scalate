package ${package}.resources

import javax.ws.rs.Path

import com.sun.jersey.api.view.ImplicitProduces

/**
 * The root resource bean
 */
@Path("/")
@ImplicitProduces(Array("text/html;qs=5"))
class HomeResource {
  
}