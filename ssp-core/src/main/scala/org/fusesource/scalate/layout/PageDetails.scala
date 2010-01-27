package org.fusesource.scalate.layout

import com.opensymphony.module.sitemesh.Page

/**
 * @version $Revision : 1.1 $
 */

class PageDetails(page: Page) {
  def title = page.getTitle

  def body = page.getBody

  def contentLength = page.getContentLength

  def pageContents = page.getPage

  def properties = page.getProperties

  def property(name: String) = page.getProperty(name)

  def booleanProperty(name: String) = page.getBooleanProperty(name)

  def intProperty(name: String) = page.getIntProperty(name)

  def longProperty(name: String) = page.getLongProperty(name)

  // TODO add helper methods for accessing meta & body properteis
}