package org.fusesource.scalate.samples.bookstore.resources

import org.fusesource.scalate.samples.bookstore.model.Book
import org.fusesource.scalate.rest.{Container, ElementResource}

/**
 * @version $Revision : 1.1 $
 */
class BookResource(val book: Book, val container: Container[String, Book])
        extends ElementResource[String, Book](book, container) with DefaultRepresentations {
}