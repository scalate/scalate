package org.fusesource.scalate.samples.scuery.resources

import org.fusesource.scalate.jersey.ScueryView
import org.fusesource.scalate.rest.{MapContainer, ContainerResource}
import org.fusesource.scalate.samples.scuery.model.Book

import javax.ws.rs.{GET, Path, Produces}
import org.fusesource.scalate.scuery.{NestedTransformer, Transformer}

/**
 * @version $Revision : 1.1 $
 */
@Path("/")
class Bookstore extends ContainerResource[String, Book, BookResource] with ScueryView {

  @GET
  @Produces(Array("text/html"))
  def index = render(new Transformer {
    $("ul.books").contents {
      node =>
        // TODO replace with a simple li:first selector!
        val li = (node \ "li")(0)

        books.flatMap {
          book =>
            transform(li) {
              $ =>
                $("a").attribute("href", "/id/" + book.id)
                $("a.book").contents = book.title
            }
        }
    }
  })

  // Container implementation
  //-------------------------------------------------------------------------

  val container = new MapContainer[String, Book]() {
    def key(book: Book) = book.id

    put(Book("item1", "Title1", "Author1"), Book("item2", "Title2", "Author2"))
  }

  def createChild(e: Book) = new BookResource(e, container)

  def books: Seq[Book] = container.map.valuesIterator.toSeq

}