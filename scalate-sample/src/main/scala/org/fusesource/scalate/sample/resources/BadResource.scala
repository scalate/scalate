package org.fusesource.scalate.sample.resources

import _root_.javax.ws.rs.Path

/**
 * A resource which creates a compile error when rendering... 
 *
 * @version $Revision: 1.1 $
 */
@Path("/bad")
class BadResource extends DefaultRepresentations {
  
}