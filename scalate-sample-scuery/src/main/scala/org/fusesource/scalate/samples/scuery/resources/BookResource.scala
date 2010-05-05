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