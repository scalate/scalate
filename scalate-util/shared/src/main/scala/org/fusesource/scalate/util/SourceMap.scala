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

import slogging.StrictLogging

import java.util.ArrayList
import scala.collection.JavaConverters._
import scala.language.reflectiveCalls
import scala.util.parsing.combinator._
import scala.util.parsing.input._
import collection.JavaConverters._
import scala.language.reflectiveCalls

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 * @author Jayson Falkner
 * @author Shawn Bayern
 */
class SourceMapStratum(val name: String) {
  var files = new ArrayList[(String, String)]()
  val lines = new ArrayList[LineInfo]()

  def mapToStratum(line: Int): Option[(String, Int)] = {

    val lines = this.lines.asScala

    def file(id: Int) = {
      val value = files.get(id)
      if (value._2 == null) value._1 else value._2
    }

    val rc = lines.filter(_.containsOutputLine(line)).map(x => (file(x.file), x.mapOutputLine(line)))
    rc.headOption
  }

  /**
   * Adds record of a new file, by filename.
   *
   * @param filename the filename to add, unqualified by path.
   */
  def addFile(filename: String): Int = addFile(filename, null)

  /**
   * Adds record of a new file, by filename and path.  The path
   * may be relative to a source compilation path.
   *
   * @param filename the filename to add, unqualified by path
   * @param filePath the path for the filename, potentially relative
   *                 to a source compilation path
   */
  def addFile(filename: String, filePath: String): Int = {
    val rc = files.size
    files.add((filename, filePath))
    rc
  }

  /**
   * Adds complete information about a simple line mapping.  Specify
   * all the fields in this method; the back-end machinery takes care
   * of printing only those that are necessary in the final SMAP.
   *
   * @param istart starting line in the source file
   *        (SMAP <tt>InputStartLine</tt>)
   * @param file the filepath (or name) from which the input comes
   *        (yields SMAP <tt>LineFileID</tt>)  Use unqualified names
   *        carefully, and only when they uniquely identify a file.
   * @param icount the number of lines in the input to map
   *        (SMAP <tt>LineFileCount</tt>)
   * @param oline starting line in the output file
   *        (SMAP <tt>OutputStartLine</tt>)
   * @param oincrement number of output lines to map to each
   *        input line (SMAP <tt>OutputLineIncrement</tt>).  <i>Given the
   *        fact that the name starts with "output", I continuously have
   *        the subconscious urge to call this field
   *        <tt>OutputLineExcrement</tt>.</i>
   */
  def addLine(istart: Int, ifile: Int, icount: Int, oline: Int, oincrement: Int): Unit = {

    if (ifile < 0 || files.size <= ifile)
      throw new IllegalArgumentException("ifile: " + ifile)
    lines.add(new LineInfo(istart, oline, ifile, icount, oincrement))
  }

  /**
   * Combines consecutive LineInfos wherever possible
   */
  def optimize() = {

    //Incorporate each LineInfo into the previous LineInfo's
    //outputLineIncrement, if possible
    var i = 0
    while (i < lines.size() - 1) {
      val li = lines.get(i)
      val liNext = lines.get(i + 1)
      if (li.file == liNext.file
        && liNext.istart == li.istart
        && liNext.icount == 1
        && li.icount == 1
        && liNext.ostart == li.ostart + li.icount * li.oincrement) {

        li.oincrement = liNext.ostart - li.ostart + liNext.oincrement
        lines.remove(i + 1)
      } else {
        i += 1
      }
    }

    //Incorporate each LineInfo into the previous LineInfo's
    //inputLineCount, if possible
    i = 0
    while (i < lines.size() - 1) {
      val li = lines.get(i)
      val liNext = lines.get(i + 1)
      if (li.file == liNext.file
        && liNext.istart == li.istart + li.icount
        && liNext.oincrement == li.oincrement
        && liNext.ostart
        == li.ostart
        + li.icount * li.oincrement) {
        li.icount += liNext.icount
        lines.remove(i + 1)
      } else {
        i += 1
      }
    }
  }

  /**
   * Returns the given stratum as a String:  a StratumSection,
   * followed by at least one FileSection and at least one LineSection.
   */
  override def toString(): String = {

    // check state and initialize buffer
    if (files.size == 0 || lines.size == 0)
      return null

    val out = new StringBuilder()

    // print StratumSection
    out.append(s"*S $name\n")

    // print FileSection
    out.append("*F\n")

    for (i <- 0 until files.size) {
      val (file, filePath) = files.get(i)
      if (filePath != null) {
        out.append(s"+ $i $file\n")
        out.append(filePath.stripPrefix("/") + "\n")
      } else {
        out.append(s"$i $file\n")
      }
    }

    // print LineSection
    out.append("*L\n")
    var lastFile = 0
    for (i <- 0 until lines.size()) {
      val line = lines.get(i)
      out.append(line.toString(lastFile))
      lastFile = line.file
    }
    out.toString()
  }

  /**
   * Represents a single LineSection in an SMAP, associated with
   * a particular stratum.
   */
  class LineInfo(var istart: Int, var ostart: Int, var file: Int, var icount: Int = 1, var oincrement: Int = 1) {
    check(istart > 0)
    check(file >= 0)
    check(ostart > 0)
    check(icount > 0)
    check(oincrement >= 0)

    def containsOutputLine(line: Int) = {
      val oend = ostart + (icount * oincrement)
      ostart <= line && line < oend
    }

    def mapOutputLine(line: Int) = {
      istart + ((line - ostart) / oincrement)
    }

    private def check(proc: => Boolean) = {
      if (!proc)
        throw new IllegalArgumentException()
    }

    override def toString() = toString(-1)

    def toString(lastFile: Int) = {
      if (istart == -1 || ostart == -1)
        throw new IllegalStateException()

      val out = new StringBuilder()
      out.append(istart)

      if (file != lastFile)
        out.append("#" + file)

      if (icount != 1)
        out.append("," + icount)

      out.append(":" + ostart)

      if (oincrement != 1)
        out.append("," + oincrement)

      out.append('\n')
      out.toString()
    }
  }

}

/**
 * Represents a source map (SMAP), which serves to associate lines
 * of the input JSP file(s) to lines in the generated servlet in the
 * final .class file, according to the JSR-045 spec.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 * @author Shawn Bayern
 */
class SourceMap extends StrictLogging {

  private[this] var outputFileName: String = null
  private[this] var doEmbedded = true
  private[this] var embedded = List[String]()
  private[this] var strata = List[SourceMapStratum]()
  private[this] var defaultStratum = "Java"

  /**
   * Adds the given string as an embedded SMAP with the given stratum name.
   *
   * @param smap the SMAP to embed
   * @param stratumName the name of the stratum output by the compilation
   *                    that produced the <tt>smap</tt> to be embedded
   */
  def addSmap(smap: String, stratumName: String): Unit = {
    val value = s"*O $stratumName\n$smap*C $stratumName\n"
    embedded = value :: embedded
  }

  /**
   * Adds the given SourceMapStratum object, representing a Stratum with
   * logically associated FileSection and LineSection blocks, to
   * the current SourceMap.  If <tt>default</tt> is true, this
   * stratum is made the default stratum, overriding any previously
   * set default.
   *
   * @param stratum the SourceMapStratum object to add
   * @param defaultStratum if <tt>true</tt>, this SourceMapStratum is considered
   *                to represent the default SMAP stratum unless
   *                overwritten
   */
  def addStratum(stratum: SourceMapStratum, defaultStratum: Boolean): Unit = {
    strata = stratum :: strata
    if (defaultStratum) this.defaultStratum = stratum.name
  }

  /**
   * Sets the filename (without path information) for the generated
   * source file.  E.g., "foo$jsp.java".
   */
  def setOutputFileName(x: String): Unit = {
    outputFileName = x
  }

  /**
   * Instructs the SourceMap whether to actually print any embedded
   * SMAPs or not.  Intended for situations without an SMAP resolver.
   *
   * @param status If <tt>false</tt>, ignore any embedded SMAPs.
   */
  def setDoEmbedded(status: Boolean): Unit = {
    doEmbedded = status
  }

  def mapToStratum(line: Int, name: String = defaultStratum) = {
    val matches = strata.find(_.name == name)
    if (matches.isDefined) {
      matches.get.mapToStratum(line)
    } else {
      None
    }
  }

  override def toString(): String = {
    if (outputFileName == null) throw new IllegalStateException
    val out: StringBuilder = new StringBuilder
    out.append("SMAP\n")
    out.append(outputFileName + '\n')
    out.append(defaultStratum + '\n')
    if (doEmbedded) {
      embedded.foreach { out.append(_) }
    }
    strata.foreach { out.append(_) }
    out.append("*E\n")
    out.toString
  }

}
object SourceMap {

  object SmapParser extends RegexParsers {

    override def skipWhitespace = false

    val number = """[0-9]+""".r ^^ { Integer.parseInt(_) }
    val nl = """\r?\n""".r
    val dot = """[^\r\n]+""".r

    val stratum: Parser[SourceMapStratum] =
      ("*S " ~> dot <~ nl) ~
        opt(
          "*F" ~ nl ~>
            rep1(
              ("+ " ~> number <~ " ") ~ (dot <~ nl) ~ (dot <~ nl) ^^ {
                case n ~ name ~ path => (n, name, path)
              } | (number <~ " ") ~ dot <~ nl ^^ {
                case n ~ name => (n, name, null)
              }) ^^ {
                case list =>
                  var rc = Map[Int, (String, String)]()
                  list.foreach {
                    case (n, name, path) => {
                      rc += n -> ((name, path))
                    }
                  }
                  rc
              }) ~
          opt(
            "*L" ~ nl ~>
              rep1(number ~ opt("#" ~> number) ~ opt("," ~> number) ~ ":" ~ number ~ opt("," ~> number) <~ nl ^^ {
                case istart ~ file ~ icount ~ ":" ~ ostart ~ oincrement =>
                  (istart, file, icount.getOrElse(1), ostart, oincrement.getOrElse(1))
              })) ^^ {
              case (name) ~ ofiles ~ lines =>
                val rc = new SourceMapStratum(name)
                var files = Map(0 -> 0)
                if (ofiles.isDefined) {
                  files = ofiles.get.transform {
                    case (index, value) =>
                      rc.addFile(value._1, value._2)
                  }
                }
                if (lines.isDefined) {
                  var lastFile = 0
                  lines.get.foreach {
                    case (istart, file, icount, ostart, oincrement) =>
                      val f = if (file.isDefined) {
                        lastFile = files.get(file.get).get
                        lastFile
                      } else {
                        lastFile
                      }
                      rc.addLine(istart, f, icount, ostart, oincrement)
                  }
                }
                rc
            }

    val smap_header: Parser[(String ~ Option[String])] = "SMAP" ~ nl ~> (dot <~ nl) ~ opt(dot <~ nl)

    val smap = smap_header ~ rep(stratum) <~ "*E" ~ nl ^^ {
      case (outputFileName ~ defaultStratum) ~ stratums =>
        val rc = new SourceMap()
        rc.setOutputFileName(outputFileName)
        stratums.foreach {
          case stratum: SourceMapStratum =>
            val isDefault = stratum.name == defaultStratum.get
            rc.addStratum(stratum, isDefault)
        }
        rc
    }

    def parse(in: String) = {
      var content = in
      if (!in.endsWith("\n")) {
        content = in + "\n"
      }
      val x = phrase(smap)(new CharSequenceReader(content))
      x match {
        case Success(result, _) => result
        case NoSuccess(message, next) => null
      }
    }

  }

  def parse(in: String) = SmapParser.parse(in)

}
