package org.fusesource.scalate.sample.resources

import _root_.com.sun.jersey.api.view.{ImplicitProduces, Viewable}
import javax.ws.rs.{Produces, GET, Path, PathParam}

/**
 * A JAX-RS bean
 *
 * @version $Revision : 1.1 $
 */
@Path("/mustache")
class MustacheResource extends DefaultRepresentations {

  def name = "James"
  
  def languages = List(Language("Scala", "Great"), Language("Java", "Crufty"))
}

case class Language(lang: String, comment: String)