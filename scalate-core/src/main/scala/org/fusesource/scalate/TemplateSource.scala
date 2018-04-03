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
package org.fusesource.scalate

import java.io.File
import support._
import util._
import util.Strings.isEmpty
import io.Source
import java.net.{ URI, URL }
import java.util.regex.Pattern

/**
 * Represents the source of a template
 *
 * @version $Revision : 1.1 $
 */
trait TemplateSource extends Resource {
  import TemplateSource.log._

  var engine: TemplateEngine = _
  private var _packageName: String = ""
  private var _simpleClassName: String = _

  /**
   * Returns the type of the template (ssp, scaml, mustache etc).
   *
   * By default the extension is extracted from the uri but custom implementations
   * can override this so that a uri could be "foo.html" but the extension overriden to be "mustache"
   * for example
   */
  def templateType: Option[String] = {
    val t = uri.split("\\.")
    if (t.length < 2) {
      None
    } else {
      Some(t.last)
    }
  }

  /**
   * Returns a new TemplateSource which uses the given template type irrespective of the actual uri file extension
   *
   * For example this lets you load a TemplateSource then convert it to be
   * of a given fixed type of template as follows:
   *
   * <code>TemplateSource.fromFile("foo.txt").templateType("mustache")</code>
   */
  def templateType(extension: String) = new CustomExtensionTemplateSource(this, extension)

  /**
   * Returns the package name the generated template class will be in for code generated templates
   */
  def packageName: String = {
    checkInitialised()
    if (engine.packagePrefix.length == 0 || _packageName.length == 0) {
      engine.packagePrefix + _packageName
    } else {
      engine.packagePrefix + "." + _packageName
    }
  }

  /**
   * Returns the generated fully qualified class name for code generated templates
   */
  def className: String = {
    val pn = packageName
    if (pn.length == 0) {
      _simpleClassName
    } else {
      pn + "." + _simpleClassName
    }
  }

  /**
   * Returns the generated simple class name (i.e. without the package name) for code generated templates
   */
  def simpleClassName: String = {
    checkInitialised()
    _simpleClassName
  }

  /**
   * Checks that we have lazily created the package and class names
   */
  protected def checkInitialised(): Unit = {
    if (_simpleClassName == null) {
      // TODO is there a nice way to assign to fields from tuple matching???
      val (pn, sn) = extractPackageAndClassNames(uri)
      _simpleClassName = sn
      _packageName = Option(pn).getOrElse("")
    }
  }

  protected def extractPackageAndClassNames(uri: String): (String, String) = {

    def processClassName(cn: String) = cn.replace('.', '_').replace("-", "$dash")

    def invalidPackageName(name: String): Boolean = isEmpty(name) || reservedWords.contains(name) || name(0).isDigit || name(0) == '_'

    val normalizedURI: String = try {
      new URI(uri).normalize.toString
    } catch {
      // on windows we can't create a URI from files named things like C:/Foo/bar.ssp
      case e: Exception =>
        val name = new File(uri).getCanonicalPath
        val sep = File.pathSeparator
        if (sep != "/") {
          // on windows lets replace the \ in a directory name with /
          val newName = name.replace('\\', '/')
          debug("converted windows path into: " + newName)
          newName
        } else {
          name
        }
    }
    val SPLIT_ON_LAST_SLASH_REGEX = Pattern.compile("^(.*)/([^/]*)$")
    val matcher = SPLIT_ON_LAST_SLASH_REGEX.matcher(normalizedURI.toString)
    if (matcher.matches == false) {
      // lets assume we have no package then
      val cn = "$_scalate_$" + processClassName(normalizedURI)
      ("", cn)
    } else {
      val unsafePackageNameWithWebInf = matcher.group(1).replaceAll("[^A-Za-z0-9_/]", "_").replaceAll("/", ".").replaceFirst("^\\.", "")

      // lets remove WEB-INF from the first name, since we should consider stuff in WEB-INF/org/foo as being in package org.foo
      val unsafePackageName = unsafePackageNameWithWebInf.stripPrefix("WEB_INF.")

      var packages = unsafePackageName.split("\\.")

      // lets find the tail of matching package names to use
      val lastIndex = packages.lastIndexWhere(invalidPackageName(_))
      if (lastIndex > 0) {
        packages = packages.drop(lastIndex + 1)
      }
      val packageName = packages.mkString(".")

      val cn = "$_scalate_$" + processClassName(matcher.group(2))
      (packageName, cn)
    }
  }

  protected val reservedWords = Set[String](
    "package", "class", "trait", "if", "else", "while", "def", "extends", "val", "var")
}

/**
 * Helper methods to create a [[org.fusesource.scalate.TemplateSource]] from various sources
 */
object TemplateSource {
  val log = Log(getClass)

  /**
   * Creates a [[org.fusesource.scalate.TemplateSource]] from the actual String contents using the given
   * URI.
   *
   * The URI is used to determine the package name to put the template in along with
   * the template kind (using the extension of the URI)
   */
  def fromText(uri: String, templateText: String) = new StringTemplateSource(uri, templateText)

  /**
   * Creates a [[org.fusesource.scalate.TemplateSource]] from a local URI such as in a web application using the
   * class loader to resolve URIs to actual resources
   */
  def fromUri(uri: String, resourceLoader: ResourceLoader) = new UriTemplateSource(uri, resourceLoader)

  /**
   * Creates a [[org.fusesource.scalate.TemplateSource]] from a file
   */
  def fromFile(file: File): FileTemplateSource = fromFile(file, file.getPath)

  def fromFile(file: File, uri: String): FileTemplateSource = new FileTemplateSource(file, uri)

  /**
   * Creates a [[org.fusesource.scalate.TemplateSource]] from a file name
   */
  def fromFile(fileName: String): FileTemplateSource = fromFile(new File(fileName))

  /**
   * Creates a [[org.fusesource.scalate.TemplateSource]] from a URL
   */
  def fromURL(url: URL): URLTemplateSource = new URLTemplateSource(url)

  /**
   * Creates a [[org.fusesource.scalate.TemplateSource]] from a URL
   */
  def fromURL(url: String): URLTemplateSource = fromURL(new URL(url))

  /**
   * Creates a [[org.fusesource.scalate.TemplateSource]] from the [[scala.io.Source]] and the given URI.
   *
   * The URI is used to determine the package name to put the template in along with
   * the template kind (using the extension of the URI)
   */
  def fromSource(uri: String, source: Source) = new SourceTemplateSource(uri, source)

}
