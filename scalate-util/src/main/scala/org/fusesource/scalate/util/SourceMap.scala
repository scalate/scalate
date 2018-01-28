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

import java.util.ArrayList
import java.io._
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
    if (rc.isEmpty) None else Some(rc.head)
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
      throw new IllegalArgumentException("ifile: " + ifile);
    lines.add(new LineInfo(istart, oline, ifile, icount, oincrement));
  }

  /**
   * Combines consecutive LineInfos wherever possible
   */
  def optimize() = {

    //Incorporate each LineInfo into the previous LineInfo's
    //outputLineIncrement, if possible
    var i = 0;
    while (i < lines.size() - 1) {
      var li = lines.get(i);
      var liNext = lines.get(i + 1);
      if (li.file == liNext.file
        && liNext.istart == li.istart
        && liNext.icount == 1
        && li.icount == 1
        && liNext.ostart == li.ostart + li.icount * li.oincrement) {

        li.oincrement = liNext.ostart - li.ostart + liNext.oincrement
        lines.remove(i + 1);
      } else {
        i += 1;
      }
    }

    //Incorporate each LineInfo into the previous LineInfo's
    //inputLineCount, if possible
    i = 0;
    while (i < lines.size() - 1) {
      var li = lines.get(i);
      var liNext = lines.get(i + 1);
      if (li.file == liNext.file
        && liNext.istart == li.istart + li.icount
        && liNext.oincrement == li.oincrement
        && liNext.ostart
        == li.ostart
        + li.icount * li.oincrement) {
        li.icount += liNext.icount
        lines.remove(i + 1);
      } else {
        i += 1;
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
      return null;

    var out = new StringBuilder();

    // print StratumSection
    out.append("*S " + name + "\n");

    // print FileSection
    out.append("*F\n")

    for (i <- 0 until files.size) {
      val (file, filePath) = files.get(i)
      if (filePath != null) {
        out.append("+ " + i + " " + file + "\n");
        out.append(filePath.stripPrefix("/") + "\n");
      } else {
        out.append(i + " " + file + "\n");
      }
    }

    // print LineSection
    out.append("*L\n");
    var lastFile = 0
    for (i <- 0 until lines.size()) {
      var line = lines.get(i);
      out.append(line.toString(lastFile));
      lastFile = line.file
    }
    out.toString();
  }

  /**
   * Represents a single LineSection in an SMAP, associated with
   * a particular stratum.
   */
  class LineInfo(var istart: Int, var ostart: Int, var file: Int, var icount: Int = 1, var oincrement: Int = 1) {
    check(istart > 0);
    check(file >= 0);
    check(ostart > 0);
    check(icount > 0);
    check(oincrement >= 0);

    def containsOutputLine(line: Int) = {
      var oend = ostart + (icount * oincrement)
      ostart <= line && line < oend
    }

    def mapOutputLine(line: Int) = {
      istart + ((line - ostart) / oincrement)
    }

    private def check(proc: => Boolean) = {
      if (!proc)
        throw new IllegalArgumentException();
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

      out.append('\n');
      out.toString();
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
class SourceMap {
  private var outputFileName: String = null
  private var doEmbedded = true
  private var embedded = List[String]()
  private var strata = List[SourceMapStratum]()
  private var defaultStratum = "Java"

  /**
   * Adds the given string as an embedded SMAP with the given stratum name.
   *
   * @param smap the SMAP to embed
   * @param stratumName the name of the stratum output by the compilation
   *                    that produced the <tt>smap</tt> to be embedded
   */
  def addSmap(smap: String, stratumName: String): Unit = {
    val value = "*O " + stratumName + "\n" + smap + "*C " + stratumName + "\n"
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
    val matches = strata.find(_.name == name);
    if (matches.isDefined) {
      matches.get.mapToStratum(line)
    } else {
      None
    }
  }

  override def toString(): String = {
    if (outputFileName == null) throw new IllegalStateException
    var out: StringBuilder = new StringBuilder
    out.append("SMAP\n")
    out.append(outputFileName + '\n')
    out.append(defaultStratum + '\n')
    if (doEmbedded) {
      embedded.foreach { out.append(_) }
    }
    strata.foreach { out.append(_) }
    out.append("*E\n")
    return out.toString
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
              }
            ) ^^ {
                case list =>
                  var rc = Map[Int, (String, String)]()
                  list.foreach {
                    case (n, name, path) => {
                      rc += n -> ((name, path))
                    }
                  }
                  rc
              }
        ) ~
          opt(
            "*L" ~ nl ~>
              rep1(number ~ opt("#" ~> number) ~ opt("," ~> number) ~ ":" ~ number ~ opt("," ~> number) <~ nl ^^ {
                case istart ~ file ~ icount ~ ":" ~ ostart ~ oincrement =>
                  (istart, file, icount.getOrElse(1), ostart, oincrement.getOrElse(1))
              })
          ) ^^ {
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
      var content = in;
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

/**
 * Conversion from Java to Scala broke something.. need to dig into this guy
 * a little more before removing the SDEInstaller
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 * @author Robert Field
 */
object SourceMapInstaller {

  /**
   * By default we only store smaps that are <= than 65535
   * due to http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6294277
   * if your JVM support larger values, just set the SOURCE_DEBUG_EXTENSION_MAX_SIZE system property.
   */
  val SOURCE_DEBUG_EXTENSION_MAX_SIZE = Integer.getInteger("SOURCE_DEBUG_EXTENSION_MAX_SIZE", 65535).intValue

  object Writer extends Log
  class Writer(val orig: Array[Byte], val sourceDebug: String) {
    import Writer._

    val bais = new ByteArrayInputStream(orig)
    val dis = new DataInputStream(bais)
    val baos = new ByteArrayOutputStream(orig.length + (sourceDebug.length * 2) + 100) {
      def position: Int = count
      def update(location: Int)(proc: => Unit): Unit = {
        val original: Int = count
        count = location
        proc
        count = original
      }
    }
    val dos = new DataOutputStream(baos)
    var sdeIndex = -1

    def copy(count: Int): Unit = {
      var i: Int = 0
      while (i < count) {
        dos.writeByte(dis.readByte)
        i += 1
      }
    }

    def copyShort() = {
      val rc = dis.readShort
      dos.writeShort(rc)
      rc
    }

    def store: Array[Byte] = {
      copy(4 + 2 + 2)
      val constantPoolCountPos: Int = baos.position
      var constantPoolCount: Int = copyShort & 0xFFFF
      sdeIndex = copyConstantPool(constantPoolCount)
      if (sdeIndex < 0) {
        writeSourceDebugConstant
        sdeIndex = constantPoolCount
        constantPoolCount += 1
        baos.update(constantPoolCountPos) {
          dos.writeShort(constantPoolCount)
        }
      }
      copy(2 + 2 + 2)
      val interfaceCount = copyShort()
      copy(interfaceCount * 2)
      copyMembers
      copyMembers
      val attrCountPos: Int = baos.position
      var attrCount: Int = dis.readShort
      dos.writeShort(attrCount)
      if (!copyAttrs(attrCount)) {
        attrCount += 1
        baos.update(attrCountPos) {
          dos.writeShort(attrCount)
        }
      }
      writeSourceDebugAttribute(sdeIndex)
      baos.toByteArray
    }

    def copyMembers(): Unit = {
      val count: Int = dis.readShort
      dos.writeShort(count)
      var i: Int = 0
      while (i < count) {
        copy(6)
        copyAttrs(copyShort())
        i += 1
      }
    }

    def copyConstantPool(constantPoolCount: Int): Int = {
      var sdeIndex: Int = -1
      var i: Int = 1
      while (i < constantPoolCount) {
        var tag: Int = dis.readByte
        dos.writeByte(tag)
        tag match {
          case 16 | 8 | 7 =>
            copy(2)
          case 15 =>
            copy(3)
          case 9 | 10 | 11 | 3 | 4 | 12 | 18 =>
            copy(4)
          case 5 | 6 =>
            copy(8)
            i += 1
          case 1 =>
            var len: Int = copyShort & 0xFFFF
            if (len < 0) {
              warn("Index is " + len + " for constantPoolCount: " + constantPoolCount + " nothing to write")
              len = 0
            }
            val data = new Array[Byte](len)
            dis.readFully(data)
            val str: String = new String(data, "UTF-8")
            if (str.equals(nameSDE)) {
              sdeIndex = i
            }
            dos.write(data)
          case _ =>
            throw new IOException("unexpected tag: " + tag)
        }
        i += 1
      }
      return sdeIndex
    }

    def copyAttrs(attrCount: Int): Boolean = {
      var sdeFound: Boolean = false
      var i: Int = 0
      while (i < attrCount) {
        val nameIndex: Int = dis.readShort
        if (nameIndex == sdeIndex) {
          sdeFound = true
        } else {
          dos.writeShort(nameIndex)
          val len = dis.readInt
          dos.writeInt(len)
          copy(len)
        }
        i += 1
      }
      return sdeFound
    }

    def writeSourceDebugAttribute(index: Int): Unit = {
      dos.writeShort(index)
      val data = sourceDebug.getBytes("UTF-8")
      dos.writeInt(data.length)
      dos.write(data)
    }

    def writeSourceDebugConstant(): Unit = {
      val len: Int = nameSDE.length
      dos.writeByte(1)
      dos.writeShort(len)
      var i: Int = 0
      while (i < len) {
        dos.writeByte(nameSDE.charAt(i))
        i += 1
      }
    }
  }

  object Reader extends Log
  class Reader(val orig: Array[Byte]) {

    val bais = new ByteArrayInputStream(orig)
    val dis = new DataInputStream(bais)

    def load: String = {
      dis.skip(4 + 2 + 2)
      val constants = readConstantPoolStrings()
      val sdeIndex = constants.get(nameSDE)
      if (sdeIndex.isEmpty) {
        return null
      }

      dis.skip(2 + 2 + 2)
      val interfaceCount = dis.readShort
      dis.skip(interfaceCount * 2)
      skipMembers
      skipMembers

      val attrbute = readAttributes().get(sdeIndex.get)
      new String(attrbute.get, "UTF-8")
    }

    def readConstantPoolStrings(): Map[String, Short] = {
      var rc = Map[String, Short]()
      var i = 1
      val count = dis.readShort & 0xFFFF
      while (i < count) {
        val tag = dis.readByte
        tag match {
          case 8 | 7 =>
            dis.skip(2)
          case 9 | 10 | 11 | 3 | 4 | 12 =>
            dis.skip(4)
          case 5 | 6 =>
            dis.skip(8)
            i += 1;
          case 1 =>
            val len: Int = dis.readShort & 0xFFFF
            val data = new Array[Byte](len)
            dis.readFully(data)
            val str: String = new String(data, "UTF-8")
            rc += (str -> i.toShort)
          case _ =>
            throw new IOException("unexpected tag: " + tag)
        }
        i += 1
      }
      return rc
    }

    def skipMembers(): Unit = {
      val count = dis.readShort
      var i: Int = 0
      while (i < count) {
        dis.skip(6)
        readAttributes()
        i += 1
      }
    }

    def readAttributes(): Map[Short, Array[Byte]] = {
      var rc = Map[Short, Array[Byte]]()
      val count = dis.readShort
      var i: Int = 0
      while (i < count) {
        val index = dis.readShort
        val len: Int = dis.readInt
        val data = new Array[Byte](len)
        dis.readFully(data)
        rc += (index -> data)
        i += 1;
      }
      return rc
    }
  }

  val nameSDE = "SourceDebugExtension";

  def load(classFile: File): String = {
    load(read(classFile))
  }

  def load(classFile: Array[Byte]): String = {
    (new Reader(classFile)).load
  }

  def store(classFile: File, sourceDebug: File): Unit = {
    store(classFile, readText(sourceDebug))
  }

  def store(classFile: File, sourceDebug: String): Unit = {
    var tmpFile = new File(classFile.getPath() + "tmp");
    store(classFile, sourceDebug, tmpFile);
    if (!classFile.delete()) {
      throw new IOException("temp file delete failed");
    }
    if (!tmpFile.renameTo(classFile)) {
      throw new IOException("temp file rename failed");
    }
  }

  def store(input: File, sourceDebug: String, output: File): Unit = {
    store(read(input), sourceDebug, output)
  }

  def store(input: File, sourceDebug: File, output: File): Unit = {
    store(read(input), readText(sourceDebug), output)
  }

  def store(input: Array[Byte], sourceDebug: String, output: File): Unit = {
    IOUtil.writeBinaryFile(output, store(input, sourceDebug))
  }

  def store(input: Array[Byte], sourceDebug: String): Array[Byte] = {

    val bytes = sourceDebug.getBytes("UTF-8")
    if (bytes.length <= SOURCE_DEBUG_EXTENSION_MAX_SIZE) {
      (new Writer(input, sourceDebug)).store
    } else {
      input
    }

  }

  private def readText(input: File) = {
    new String(read(input), "UTF-8")
  }
  private def read(input: File) = {
    if (!input.exists()) {
      throw new FileNotFoundException("no such file: " + input);
    }
    IOUtil.loadBinaryFile(input)
  }

  def main(args: Array[String]) = {
    val smap1 = new SourceMap()
    smap1.setOutputFileName("foo.scala");
    val straturm = new SourceMapStratum("JSP")
    straturm.addFile("foo.scala", "path/to/foo.scala")
    straturm.addLine(1, 0, 1, 2, 1);
    straturm.addLine(2, 0, 1, 3, 1);
    straturm.addLine(4, 0, 1, 8, 1);
    straturm.addLine(5, 0, 1, 9, 1);
    smap1.addStratum(straturm, true)
    val text1 = smap1.toString
    println(text1)

    val smap2 = SourceMap.parse(text1)
    val text2 = smap2.toString
    println(text2)

    println(text2 == text1)

    println(smap2.mapToStratum(3));

  }
}
