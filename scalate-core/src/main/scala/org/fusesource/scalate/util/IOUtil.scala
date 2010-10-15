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

import java.io._
import java.util.zip.{ZipEntry, ZipInputStream}
import org.fusesource.scalate.support.FileResource
import java.net.URL

object IOUtil extends Logging {

  /**
   * Allows a File to be converted to a FileResource which also provides a Rich API for files
   */
  implicit def toResource(file: File) = new FileResource(file, file.getPath)
  implicit def toFile(resource: FileResource): File = resource.asFile

  /**
   * Creates any parent directories of the given path if they do not exist
   */
  def makeParentDirs(fileName: String): Unit = makeParentDirs(new File(fileName))

  /**
   * Creates any parent directories of the given directory if they do not exist
   */
  def makeParentDirs(file: File): Unit = {
    val parent = file.getParentFile
    if (parent != null) {
      parent.mkdirs
    }
  }

  /**
   * Recursively deletes a file and all of it's children files if it's a directory.
   */
  def recursiveDelete(file: File): Boolean = {
    if (file.isDirectory) {
      val children = file.listFiles
      if (children != null) {
        for (child <- children) {
          recursiveDelete(child)
        }
      }
    }
    return file.delete
  }


  def loadText(in: InputStream, encoding: String = "UTF-8"): String = new String(loadBytes(in), encoding)

  def loadTextFile(path: File, encoding: String = "UTF-8") = new String(loadBinaryFile(path), encoding)

  def loadBinaryFile(path: File): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    val in = new FileInputStream(path)
    try {
      copy(in, baos)
    } finally {
      in.close
    }

    baos.toByteArray
  }

  def loadBytes(in: InputStream): Array[Byte] = {
    val baos = new ByteArrayOutputStream
    try {
      copy(in, baos)
    } finally {
      in.close
    }
    baos.toByteArray
  }


  def writeText(path: String, text: String): Unit = writeText(new File(path), text)

  def writeText(path: File, text: String): Unit = writeText(new FileWriter(path), text)

  def writeText(stream: OutputStream, text: String): Unit = writeText(new OutputStreamWriter(stream), text)

  def writeText(out: Writer, text: String): Unit = {
    try {
      out.write(text)
    } finally {
      out.close
    }
  }

  def writeBinaryFile(path: String, contents: Array[Byte]): Unit = writeBinaryFile(new File(path), contents)

  def writeBinaryFile(path: File, contents: Array[Byte]): Unit = {
    val out = new FileOutputStream(path)
    try {
      out.write(contents)
    } finally {
      out.close
    }
  }

  def copy(in: File, out: File): Long = {
    out.getParentFile.mkdirs
    copy(new FileInputStream(in), new FileOutputStream(out))
  }

  def copy(file: File, out: OutputStream): Long = copy(new BufferedInputStream(new FileInputStream(file)), out)

  def copy(in: InputStream, file: File): Long = {
    val out = new FileOutputStream(file)
    try {
      copy(in, out)
    } finally {
      out.close
    }
  }

  def copy(url: URL, file: File): Long = {
    val in = url.openStream
    try {
      copy(in, file)
    } finally {
      in.close
    }
  }

  def copy(in: InputStream, out: OutputStream): Long = {
    var bytesCopied: Long = 0
    val buffer = new Array[Byte](8192)

    var bytes = in.read(buffer)
    while (bytes >= 0) {
      out.write(buffer, 0, bytes)
      bytesCopied += bytes
      bytes = in.read(buffer)
    }

    bytesCopied
  }


  def copy(in: Reader, out: Writer): Long = {
    var charsCopied: Long = 0
    val buffer = new Array[Char](8192)

    var chars = in.read(buffer)
    while (chars >= 0) {
      out.write(buffer, 0, chars)
      charsCopied += chars
      chars = in.read(buffer)
    }

    charsCopied
  }

  /**
   * Unjars the given stream for entries which match the optional filter to the given directory
   */
  def unjar(outputDir: File, input: InputStream, filter: ZipEntry => Boolean = allZipEntries): Unit = {
    val zip = new ZipInputStream(input)
    try {
      val buffer = new Array[Byte](64 * 1024)
      var ok = true
      while (ok) {
        val entry = zip.getNextEntry
        if (entry == null) {
          ok = false
        }
        else {
          val name = entry.getName
          if (!entry.isDirectory && filter(entry)) {
            debug("processing resource: " + name)
            val file = new File(outputDir.getCanonicalPath + "/" + name)
            file.getParentFile.mkdirs
            val bos = new FileOutputStream(file)
            try {
              var bytes = 1
              while (bytes > 0) {
                bytes = zip.read(buffer)
                if (bytes > 0) {
                  bos.write(buffer, 0, bytes)
                }
              }
            } finally {
              bos.close
            }
          }
          zip.closeEntry
        }
      }
    }
    finally {
      zip.close
    }
  }

  /**
   * Recursively deletes the directory and all its children which match the optional filter
   */
  def recursiveDelete(file: File, filter: File => Boolean = allFiles): Unit = {
    if (file.exists) {
      if (file.isDirectory) {
        for (c <- file.listFiles) {
          recursiveDelete(c)
        }
      }
      if (filter(file)) {
        file.delete
      }
    }
  }

  protected def allZipEntries(entry: ZipEntry): Boolean = true

  protected def allFiles(file: File): Boolean = true
}