package org.fusesource.scalate.bookstore.resources

import org.fusesource.scalate.bookstore.model.Book
import org.fusesource.scalate.jersey.{MapContainer, ContainerResource}
import java.lang.String
import javax.ws.rs.{Path}

/**
 * @version $Revision: 1.1 $
 */
@Path("/")
class Bookstore extends ContainerResource[String,Book,BookResource] with DefaultRepresentations {
  val container = new MapContainer[String,Book]() {
    def key(book: Book) = book.id

    put(Book("item1", "Title1", "Author1"), Book("item2", "Title2", "Author2"))
  }

  def createChild(e: Book) = new BookResource(e, container)
}