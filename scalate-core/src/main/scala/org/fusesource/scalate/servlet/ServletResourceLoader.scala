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
import java.io.{File, InputStreamReader, StringWriter}
import org.fusesource.scalate.util.IOUtil
import org.fusesource.scalate.{FileResourceLoader, ResourceLoader, TemplateException}

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class ServletResourceLoader(context: ServletContext) extends FileResourceLoader {

  override protected def toFile(uri:String):File = {
    new File(context.getRealPath(uri))
  }

  override protected def toFileOrFail(uri:String):File = {
    val path = context.getRealPath(uri);
    if (path==null) {
      throw new TemplateException("Cannot find [" + uri + "]; are you sure it's within [" + context.getRealPath("/") + "]?")
    }
    new File(path)
  }


}
