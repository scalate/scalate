package org.fusesource.scalate.resource

import java.io.{ ByteArrayInputStream, Reader, StringReader }

abstract class TextResource extends Resource {
  override def reader: Reader = new StringReader(text)

  def inputStream = new ByteArrayInputStream(text.getBytes)

  // just return current time as we have no way to know
  def lastModified: Long = System.currentTimeMillis
}
