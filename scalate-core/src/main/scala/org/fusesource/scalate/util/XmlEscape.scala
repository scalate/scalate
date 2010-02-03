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


package org.fusesource.scalate.util


object XmlEscape
{

  def escape( text: String ): String =
    text.foldLeft( new StringBuffer )( (acc, ch) => escape( ch, acc ) ).toString


  private def escape( ch: Char, buffer: StringBuffer ): StringBuffer = {
    if( ( ch >= 0x20 && ch <= 0x21 )  || 
        ( ch >= 0x23 && ch <= 0x25 )  ||
        ( ch >= 0x28 && ch <= 0x3B )  ||
        ( ch == 0x3D )                ||
        ( ch >= 0x3F && ch <= 0x7E ) ) {
      buffer.append( ch )
    } else {
      buffer.append( ch match {
        case '"' => { "&quot;" }
        case '&' => { "&amp;" }
        case '<' => { "&lt;" }
        case '>' => { "&gt;" }
        case _   => {  "&#x" + ch.toInt.toHexString + ";" }
      })
    }
  }

}
