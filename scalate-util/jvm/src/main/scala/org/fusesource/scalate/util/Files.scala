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

  def recursiveFind(directories: Iterable[File])(filter: File => Boolean): Option[File] = {
    directories.view.map(recursiveFind(_)(filter)).find(_.isDefined).flatten
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
    } else {
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
