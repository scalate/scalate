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

package org.fusesource.scalate.samples.scuery.resources

import org.fusesource.scalate.rest.{Container, ElementResource}
import org.fusesource.scalate.scuery.Transformer
import org.fusesource.scalate.samples.scuery.model.Book

import javax.ws.rs.{GET, Path, Produces}

/**
 * @version $Revision : 1.1 $
 */
class BookResource(val book: Book, val container: Container[String, Book])
        extends ElementResource[String, Book](book, container) {
          
  @GET 
  @Produces(Array("text/html"))
  def index = new Transformer {
    $("h1.title").contents = book.title
    $(".title").contents = book.title
    $(".author").contents = book.author
    $(".id").contents = book.id
    $("a.edit").attribute("href", "/id/" + book.id + "/edit")
  }


  @GET
  @Path("edit")
  @Produces(Array("text/html"))
  def edit = index

}