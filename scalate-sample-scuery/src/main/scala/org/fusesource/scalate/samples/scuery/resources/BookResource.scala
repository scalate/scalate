package org.fusesource.scalate.samples.scuery.resources

import org.fusesource.scalate.jersey.ScueryView
import org.fusesource.scalate.rest.{Container, ElementResource}
import org.fusesource.scalate.scuery.Transformer
import org.fusesource.scalate.samples.scuery.model.Book

import javax.ws.rs.{GET, Path, Produces}
import org.fusesource.scalate.samples.scuery.ServletContextListener

/**
 * @version $Revision : 1.1 $
 */
class BookResource(val book: Book, val container: Container[String, Book])
        extends ElementResource[String, Book](book, container) with ScueryView {
          
  @GET 
  @Produces(Array("text/html"))
  def index = render(new Transformer {
    $("h1.title").contents = book.title
    $(".title").contents = book.title
    $(".author").contents = book.author
    $(".id").contents = book.id
  })


  // TODO temporary hack until we can inject sub resources!
  override protected def servletContext = {
    try {
      super.servletContext
    }
    catch {
      case e =>
        ServletContextListener()
    }
  }
}