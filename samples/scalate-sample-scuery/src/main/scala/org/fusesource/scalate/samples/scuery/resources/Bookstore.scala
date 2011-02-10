/**
 * Copyright (C) 2009-2011 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.scalate.samples.scuery.resources

import org.fusesource.scalate.rest.{MapContainer, ContainerResource}
import org.fusesource.scalate.samples.scuery.model.Book

import javax.ws.rs.{GET, Path, Produces}
import org.fusesource.scalate.scuery.Transformer

/**
 * @version $Revision : 1.1 $
 */
@Path("/")
class Bookstore extends ContainerResource[String, Book, BookResource] {

  @GET
  @Produces(Array("text/html"))
  def index = new Transformer {
    $("ul.books").contents {
      node =>
        books.flatMap {
          book =>
            transform(node.$("li:first-child")) {
              $ =>
                $("a").attribute("href", "/id/" + book.id)
                $("a.book").contents = book.title
            }
        }
    }
  }


  // Container implementation
  //-------------------------------------------------------------------------

  val container = new MapContainer[String, Book]() {
    def key(book: Book) = book.id

    put(Book("item1", "Title1", "Author1"), Book("item2", "Title2", "Author2"))
  }

  def createChild(e: Book) = new BookResource(e, container)

  def books: Seq[Book] = container.map.valuesIterator.toSeq

}