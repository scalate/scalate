/*
 * Copyright (c) 2009 Matthew Hildebrand <matt.hildebrand@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.fusesource.scalate.servlet

import javax.servlet.ServletContext
import org.fusesource.scalate.{ResourceLoader, TemplateException}
import java.io.{File, InputStreamReader, StringWriter}
import org.fusesource.scalate.util.IOUtil

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class ServletResourceLoader(context: ServletContext) extends ResourceLoader {
  val pageFileEncoding = "UTF-8"

  override def load(uri: String): String = {
    val stream = context.getResourceAsStream(uri)
    if (stream == null) {
      throw new TemplateException("Cannot find [" + uri + "]; are you sure it's within [" + context.getRealPath("/") + "]?")
    }

    val reader = new InputStreamReader(stream, pageFileEncoding)
    val writer = new StringWriter(stream.available)
    try {
      IOUtil.copy(reader, writer)
      writer.toString
    } finally {
      reader.close
    }
  }

  override def lastModified(uri:String) = new File(context.getRealPath(uri)).lastModified


}
