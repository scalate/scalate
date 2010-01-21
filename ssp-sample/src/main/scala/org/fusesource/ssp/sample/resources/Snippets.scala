package org.fusesource.ssp.sample.resources

import java.util.Date

/**
 * @version $Revision: 1.1 $
 */

object Snippets {
  def cheese = { <h1>Hello at {new Date()}</h1> <p>This is some more text</p> }

  def beer = <h3>mmm I like beer</h3>

  def itemLink(id: String, name: String) = <a href={"/foo/" + id} title={"Go to " + name}>{name}</a>
}