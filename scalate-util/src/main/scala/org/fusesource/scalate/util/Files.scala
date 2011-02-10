/**
 * Copyright (C) 2009-2011 the original author or authors.
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

import java.io.File

object Files {

  /**
   * Recursively finds the first file in this directory that matches the given
   * predicate or matches against this file for non-directories
   */
  def recursiveFind(file: File)(filter: File => Boolean): Option[File] = {
    andDescendants(file).find(filter)
/*
    if (file.isDirectory) {
      file.listFiles.view.map(recursiveFind(_)(filter)).find(_.isDefined).getOrElse(None)
    } else {
      if (filter(file)) Some(file) else None
    }
*/
  }

  def recursiveFind(directories: Traversable[File])(filter: File => Boolean): Option[File] = {
    directories.view.map(recursiveFind(_)(filter)).find(_.isDefined).getOrElse(None)
  }

  def children(file: File): Iterable[File] = new Iterable[File] {
    def iterator = if (file.isDirectory) file.listFiles.iterator else Iterator.empty
  }

  def descendants(file: File): Iterable[File] = (
    children(file).view.flatMap(andDescendants(_)))


  def andDescendants(file: File): Iterable[File] = (
    Seq(file) ++ descendants(file))


  /**
   * Returns true if
   */
  def isDescendant(root: File, file: File): Boolean = {
    val dir = if (file.isDirectory) file else file.getParentFile
    dir.getCanonicalPath.startsWith(root.getCanonicalPath)
  }

  /**
   * Returns the relative URI of the given file with the respect to the given root directory
   */
  def relativeUri(rootDir: File, file: File): String = {
    val parent = file.getParentFile
    if (parent == null || parent == rootDir) {
      file.getName
    }
    else {
      relativeUri(rootDir, parent) + "/" + file.getName
    }
  }

  /**
   * Returns the extension of the file name
   */
  def extension(name: String) = {
    val idx = name.lastIndexOf('.')
    if (idx >= 0) {
      name.substring(idx + 1)
    } else {
      ""
    }
  }

  /**
   * Returns the name of the file without its extension
   */
  def dropExtension(name: String): String = {
    val idx = name.lastIndexOf('.')
    if (idx >= 0) {
      name.substring(0, idx)
    } else {
      name
    }
  }

  /**
   * Returns the name of the file without its extension
   */
  def dropExtension(file: File): String = dropExtension(file.getName)
}

