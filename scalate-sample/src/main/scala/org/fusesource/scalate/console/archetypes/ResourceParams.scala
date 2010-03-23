package org.fusesource.scalate.console.archetypes

import _root_.org.fusesource.scalate.console.ArchetypeResource

/**
 * Parameters to create a JAXRS resource template
 *
 * @version $Revision: 1.1 $
 */
object ResourceParams {
  def apply(controller: ArchetypeResource): ResourceParams = {
    import controller._
    new ResourceParams(formParam("packageName"), formParam("className"), formParam("resourceUri"))
  }
}
case class ResourceParams(val packageName: String, val className:String, val resourceUri: String) {

  /**
   * Returns the fully qualified class name of the resource to be generated
   */
  def qualifiedClassName = packageName + "." + className
}