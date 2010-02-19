package org.fusesource.scalate.bookstore.resources

import org.fusesource.scalate.bookstore.model.Book
import org.fusesource.scalate.jersey.{Container, ElementResource}

/**
 * @version $Revision : 1.1 $
 */
class BookResource(val book: Book, val container: Container[String, Book])
        extends ElementResource[String, Book](book, container) with DefaultRepresentations {
}