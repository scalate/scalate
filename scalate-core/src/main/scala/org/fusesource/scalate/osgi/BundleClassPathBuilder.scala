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
package org.fusesource.scalate.osgi

import java.io.{InputStream, IOException, File}
import scala.reflect.io.AbstractFile
import java.net.URL
import java.lang.String
import org.osgi.framework.{ServiceReference, Bundle}
import collection.mutable.{ListBuffer,LinkedHashSet}
import org.osgi.service.packageadmin.PackageAdmin
import org.fusesource.scalate.util.{Log, Strings}

/**
 * Helper methods to transform OSGi bundles into {@link AbstractFile} implementations
 * suitable for use with the Scala compiler
 */
object BundleClassPathBuilder {
  val log = Log(getClass); import log._

  // These were removed in Scala 2.11.  We still use them.
  private trait AbstractFileCompatibility { this: AbstractFile =>
    def lookupPath(path: String, directory: Boolean): AbstractFile = {
      lookup((f, p, dir) => f.lookupName(p, dir), path, directory)
    }

    private def lookup(getFile: (AbstractFile, String, Boolean) => AbstractFile,
                       path0: String,
                       directory: Boolean): AbstractFile = {
      val separator = java.io.File.separatorChar
      // trim trailing '/'s
      val path: String = if (path0.last == separator) path0 dropRight 1 else path0
      val length = path.length()
      assert(length > 0 && !(path.last == separator), path)
      var file: AbstractFile = this
      var start = 0
      while (true) {
        val index = path.indexOf(separator, start)
        assert(index < 0 || start < index, ((path, directory, start, index)))
        val name = path.substring(start, if (index < 0) length else index)
        file = getFile(file, name, if (index < 0) directory else true)
        if ((file eq null) || index < 0) return file
        start = index + 1
      }
      file
    }
  }

  /**
   * Create a list of AbstractFile instances, representing the bundle and its wired depedencies
   */
  def fromBundle(bundle: Bundle) : List[AbstractFile] = {
    require(bundle != null, "Bundle should not be null")

    // add the bundle itself
    val files = ListBuffer(create(bundle))

    // also add all bundles that have exports wired to imports from this bundle
    files.appendAll(fromWires(bundle))

    files.toList
  }

  /**
   * Find bundles that have exports wired to the given and bundle
   */
  def fromWires(bundle: Bundle) : List[AbstractFile] = {
    debug("Checking OSGi bundle wiring for %s", bundle)
    val context = bundle.getBundleContext
    var ref: ServiceReference = context.getServiceReference(classOf[PackageAdmin].getName)

    if (ref == null) {
      warn("PackageAdmin service is unavailable - unable to check bundle wiring information")
      return List()
    }

    try {
      var admin: PackageAdmin = context.getService(ref).asInstanceOf[PackageAdmin]
      if (admin == null) {
        warn("PackageAdmin service is unavailable - unable to check bundle wiring information")
        return List()
      }
      return fromWires(admin, bundle)
    } finally {
      context.ungetService(ref)
    }
  }
                                                                                                                                                                
  def fromWires(admin: PackageAdmin, bundle: Bundle) : List[AbstractFile] = {
    val exported = admin.getExportedPackages(null : Bundle)
    val set = new LinkedHashSet[Bundle]
    for (pkg <- exported; if pkg.getExportingBundle.getBundleId != 0) {
        val bundles = pkg.getImportingBundles();
        if (bundles != null) {
            for (b <- bundles; if b.getBundleId == bundle.getBundleId) {
              debug("Bundle imports %s from %s",pkg,pkg.getExportingBundle)
              if (b.getBundleId == 0) {
                debug("Ignoring system bundle")
              } else {
                set += pkg.getExportingBundle
              }
            }
        }
    }
    set.map(create(_)).toList
  }


  /**
   *  Create a new  { @link AbstractFile } instance representing an
   * { @link org.osgi.framework.Bundle }
   *
   * @param bundle the bundle
   */
  def create(bundle: Bundle): AbstractFile = {
    require(bundle != null, "Bundle should not be null")

    abstract class BundleEntry(url: URL, parent: DirEntry) extends AbstractFile with AbstractFileCompatibility {
      require(url != null, "url must not be null")
      lazy val (path: String, name: String) = getPathAndName(url)
      lazy val fullName: String = (path::name::Nil).filter(n => !Strings.isEmpty(n)).mkString("/")

      /**
       * @return null
       */
      def file: File = null

      /**
       * @return last modification time or 0 if not known
       */
      def lastModified: Long =
        try { url.openConnection.getLastModified }
        catch { case _: Exception => 0 }

      @throws(classOf[IOException])
      def container: AbstractFile =
        valueOrElse(parent) {
          throw new IOException("No container")
        }

      @throws(classOf[IOException])
      def input: InputStream = url.openStream

      /**
       * Not supported. Always throws an IOException.
       * @throws IOException
       */
      @throws(classOf[IOException])
      def output = throw new IOException("not supported: output")

      private def getPathAndName(url: URL): (String, String) = {
        val u = url.getPath
        var k = u.length
        while( (k > 0) && (u(k - 1) == '/') )
          k = k - 1

        var j = k
        while( (j > 0) && (u(j - 1) != '/') )
          j = j - 1

        (u.substring(if (j > 0) 1 else 0, if (j > 1) j - 1 else j), u.substring(j, k))
      }

      override def toString = fullName
    }

    class DirEntry(url: URL, parent: DirEntry) extends BundleEntry(url, parent) {

      /**
       * @return true
       */
      def isDirectory: Boolean = true

      override def iterator: Iterator[AbstractFile] = {
        new Iterator[AbstractFile]() {
          val dirs = bundle.getEntryPaths(fullName)
          var nextEntry = prefetch()

          def hasNext() = {
            if (nextEntry == null)
              nextEntry = prefetch()

            nextEntry != null
          }

          def next() = {
            if (hasNext()) {
                val entry = nextEntry
                nextEntry = null
                entry
            }
            else {
              throw new NoSuchElementException()
            }
          }

          private def prefetch() = {
            if (dirs.hasMoreElements) {
              val entry = dirs.nextElement.asInstanceOf[String]
              var entryUrl = bundle.getResource("/" + entry)

              // Bundle.getResource seems to be inconsistent with respect to requiring
              // a trailing slash
              if (entryUrl == null)
                entryUrl = bundle.getResource("/" + removeTralingSlash(entry))

              // If still null OSGi wont let use load that resource for some reason
              if (entryUrl == null) {
                null
              }
              else {
                if (entry.endsWith(".class"))
                  new FileEntry(entryUrl, DirEntry.this)
                else
                  new DirEntry(entryUrl, DirEntry.this)
              }
            }
            else
              null
          }

          private def removeTralingSlash(s: String): String =
            if (s == null || s.length == 0)
              s
            else if (s.last == '/')
              removeTralingSlash(s.substring(0, s.length - 1))
            else
              s
        }
      }

      def lookupName(name: String, directory: Boolean): AbstractFile = {
        val entry = bundle.getEntry(fullName + "/" + name)
        nullOrElse(entry) { entry =>
          if (directory)
            new DirEntry(entry, DirEntry.this)
          else
            new FileEntry(entry, DirEntry.this)
        }
      }

      override def lookupPathUnchecked(path: String, directory: Boolean) = lookupPath(path, directory)
      def lookupNameUnchecked(name: String, directory: Boolean) = lookupName(path, directory)

      def absolute = unsupported("absolute() is unsupported")
      def create = unsupported("create() is unsupported")
      def delete = unsupported("create() is unsupported")
    }

    class FileEntry(url: URL, parent: DirEntry) extends BundleEntry(url, parent) {

      /**
       * @return false
       */
      def isDirectory: Boolean = false
      override def sizeOption: Option[Int] = Some(bundle.getEntry(fullName).openConnection().getContentLength())
      def lookupName(name: String, directory: Boolean): AbstractFile = null

      override def lookupPathUnchecked(path: String, directory: Boolean) = lookupPath(path, directory)
      def lookupNameUnchecked(name: String, directory: Boolean) = lookupName(path, directory)

      def iterator = Iterator.empty

      def absolute = unsupported("absolute() is unsupported")
      def create = unsupported("create() is unsupported")
      def delete = unsupported("create() is unsupported")      
    }

    new DirEntry(bundle.getResource("/"), null) {
      override def toString = "AbstractFile[" + bundle + "]"
    }
  }

  /**
   * Evaluate <code>f</code> on <code>s</code> if <code>s</code> is not null.
   * @param s
   * @param f
   * @return <code>f(s)</code> if s is not <code>null</code>, <code>null</code> otherwise.
   */
  def nullOrElse[S, T](s: S)(f: S => T): T =
    if (s == null) null.asInstanceOf[T]
    else f(s)

  /**
   * @param t
   * @param default
   * @return <code>t</code> or <code>default</code> if <code>null</code>.
   */
  def valueOrElse[T](t: T)(default: => T) =
    if (t == null) default
    else t  
}

