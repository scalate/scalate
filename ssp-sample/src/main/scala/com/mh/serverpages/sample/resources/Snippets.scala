package com.mh.serverpages.sample.resources

import java.util.Date

/**
 * @version $Revision: 1.1 $
 */

object Snippets {
  def cheese = { <h1>Hello at {new Date()}</h1> <p>This is some more text</p> }

  def itemLink(id: String, name: String) = <a href={"/foo/" + id} title={"Go to " + name}>{name}</a>
}