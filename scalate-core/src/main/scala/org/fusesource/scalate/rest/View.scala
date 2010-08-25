package org.fusesource.scalate.rest

/**
 * A helper class to represent a template view which can be passed into JAXRS for rendering
 */
case class View[T](uri: String, model: Option[T] = None) {
}