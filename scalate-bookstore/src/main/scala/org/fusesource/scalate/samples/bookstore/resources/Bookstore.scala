/**
 * Copyright (C) 2009-2010 the original author or authors.
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

package org.fusesource.scalate.samples.bookstore.resources

import org.fusesource.scalate.samples.bookstore.model.Book
import org.fusesource.scalate.rest.{MapContainer, ContainerResource}
import java.lang.String
import javax.ws.rs.{Path}
import org.fusesource.scalate.samples.bookstore.service.FooService
import com.google.inject.Inject

/**
 * @version $Revision : 1.1 $
 */
@Path("/")
class Bookstore extends ContainerResource[String, Book, BookResource] with DefaultRepresentations {

  @Inject
  private var _fooService: FooService = _

  def fooService: FooService = {
    if (_fooService == null) {
      _fooService = new FooService {
        def name = "WARN foo not injected!!!"
      }
    }
    _fooService
  }

  val container = new MapContainer[String, Book]() {
    def key(book: Book) = book.id

    put(Book("item1", "Title1", "Author1"), Book("item2", "Title2", "Author2"))
  }

  def createChild(e: Book) = new BookResource(e, container)

}