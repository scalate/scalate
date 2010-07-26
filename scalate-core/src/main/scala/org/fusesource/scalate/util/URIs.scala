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

package org.fusesource.scalate.util

/**
 * Some helper methods for working with URIs and query strings
 *
 * @version $Revision: 1.1 $
 */
object URIs {

  /**
   * Creates a URI using a path and optional query string
   */
  def uri(path: String, query: String = "") = {
    if (query != null && query.length > 0) {
      val separator = if (path.contains("?")) "&" else "?"
      path + separator + query
    }
    else {
      path
    }
  }

  /**
   * Combines the URI path, query string with additional query terms which will avoid duplicates
   */
  def uriPlus(path: String, query: String, addQuery: String)= {
    val newQuery = (splitQuery(query) ++ splitQuery(addQuery)).distinct
    uri(path, joinQuery(newQuery))

  }

  /**
   * Removes the given query terms from the query string if they are there
   */
  def uriMinus(path: String, query: String, removeQuery: String)= {
    val remove = splitQuery(removeQuery)
    val newQuery = splitQuery(query).filter(!remove.contains(_))
    uri(path, joinQuery(newQuery))
  }

  /**
   * Split a query expression into separate clauses
   */
  protected def splitQuery(query:String): Seq[String] = if (query != null && query.length > 0) query.split("&").toSeq else Nil

  protected def joinQuery(queryArgs: Seq[String]) = queryArgs.mkString("&")
}