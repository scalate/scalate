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