/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.scalate.util

import java.util.List
import java.util.ArrayList
import java.io._

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 * @author Jayson Falkner
 * @author Shawn Bayern
 */
class SourceMapStratum(val name: String) {
  var files = new ArrayList[(String, String)]()
  val lines = new ArrayList[LineInfo]()
  private var lastFile = 0;

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
   * (My view is that fields are optional primarily for spatial efficiency,
   * not for programmer convenience.  Could always add utility methods
   * later.)
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

    // build the LineInfo
    val file = if (ifile != lastFile) {
      lastFile = ifile
      Some(ifile)
    } else {
      None
    }
    lines.add(new LineInfo(istart, oline, file, icount, oincrement));
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
      if (!liNext.file.isDefined
              && liNext.istart == li.istart
              && liNext.icount == 1
              && li.icount == 1
              && liNext.ostart
              == li.ostart
              + li.icount * li.oincrement) {
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
      if (!liNext.file.isDefined
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
    for (i <- 0 until lines.size()) {
      out.append(lines.get(i));
    }
    out.toString();
  }

  /**
   * Represents a single LineSection in an SMAP, associated with
   * a particular stratum.
   */
  class LineInfo(var istart: Int, var ostart: Int, var file: Option[Int], var icount: Int = 1, var oincrement: Int = 1) {
    check(istart > 0);
    check(ostart > 0);
    check(icount > 0);
    check(oincrement >= 0);
    if (file.isDefined) {
      check(file.get >= 0);
    }

    private def check(proc: => Boolean) = {
      if (!proc)
        throw new IllegalArgumentException();
    }

    override def toString() = {
      if (istart == -1 || ostart == -1)
        throw new IllegalStateException();
      val out = new StringBuilder();
      out.append(istart);
      if (file.isDefined)
        out.append("#" + file.get);
      if (icount != 1)
        out.append("," + icount);
      out.append(":" + ostart);
      if (oincrement != 1)
        out.append("," + oincrement);
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
  private var embedded = new ArrayList[String]
  private var strata = new ArrayList[SourceMapStratum]
  private var defaultStratum = "Java"

  /**
   * Adds the given string as an embedded SMAP with the given stratum name.
   *
   * @param smap the SMAP to embed
   * @param stratumName the name of the stratum output by the compilation
   *                    that produced the <tt>smap</tt> to be embedded
   */
  def addSmap(smap: String, stratumName: String): Unit = {
    embedded.add("*O " + stratumName + "\n" + smap + "*C " + stratumName + "\n")
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
    strata.add(stratum)
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

  override def toString(): String = {

    if (outputFileName == null) throw new IllegalStateException

    var out: StringBuilder = new StringBuilder
    out.append("SMAP\n")
    out.append(outputFileName + '\n')
    out.append(defaultStratum + '\n')

    if (doEmbedded) {
      var nEmbedded: Int = embedded.size
      var i = 0
      while (i < nEmbedded) {
        out.append(embedded.get(i))
        i += 1;
      }
    }
    var nStrata: Int = strata.size

    var i = 0
    while (i < nStrata) {
      var s: SourceMapStratum = strata.get(i)
      out.append(s.toString)
      i += 1
    }

    out.append("*E\n")
    return out.toString
  }

}

/**
 * Conversion from Java to Scala broke something.. need to dig into this guy
 * a little more before removing the SDEInstaller
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 * @author Robert Field
 */
object SourceMapInstall {
  class Installer(val orig: Array[Byte], val sdeAttr: Array[Byte]) extends Logging {
    val gen: Array[Byte] = new Array[Byte](orig.length + sdeAttr.length + 100);

    var origPos = 0
    var genPos = 0
    var sdeIndex = 0

    def install: Array[Byte] = {
      copy(4 + 2 + 2)
      var constantPoolCountPos: Int = genPos
      var constantPoolCount: Int = readU2
      writeU2(constantPoolCount)
      sdeIndex = copyConstantPool(constantPoolCount)
      if (sdeIndex < 0) {
        writeUtf8ForSDE
        sdeIndex = constantPoolCount
        constantPoolCount += 1; 
        randomAccessWriteU2(constantPoolCountPos, constantPoolCount)
      }
      else {
      }
      copy(2 + 2 + 2)
      var interfaceCount: Int = readU2
      writeU2(interfaceCount)
      copy(interfaceCount * 2)
      copyMembers
      copyMembers
      var attrCountPos: Int = genPos
      var attrCount: Int = readU2
      writeU2(attrCount)
      if (!copyAttrs(attrCount)) {
        attrCount += 1
        randomAccessWriteU2(attrCountPos, attrCount)
      }
      writeAttrForSDE(sdeIndex)
      gen.slice(0, genPos)
    }


    def copyMembers: Unit = {
      var count: Int = readU2
      writeU2(count)
      var i: Int = 0
      while (i < count) {
        copy(6)
        var attrCount: Int = readU2
        writeU2(attrCount)
        copyAttrs(attrCount)
        i += 1
      }
    }


    def copyAttrs(attrCount: Int): Boolean = {
      var sdeFound: Boolean = false
      var i: Int = 0
      while (i < attrCount) {
        var nameIndex: Int = readU2
        if (nameIndex == sdeIndex) {
          sdeFound = true
        }
        else {
          writeU2(nameIndex)
          var len: Int = readU4
          writeU4(len)
          copy(len)
        }
        i += 1;
      }
      return sdeFound
    }

    def writeAttrForSDE(index: Int): Unit = {
      writeU2(index)
      writeU4(sdeAttr.length)

      var i: Int = 0
      while (i < sdeAttr.length) {
        writeU1(sdeAttr(i))
        i += 1;
      }
    }


    def randomAccessWriteU2(pos: Int, value: Int): Unit = {
      var savePos: Int = genPos
      genPos = pos
      writeU2(value)
      genPos = savePos
    }


    def readU1: Int = {
      return orig(({origPos += 1; origPos})) & 0xFF
    }


    def readU2: Int = {
      var res: Int = readU1
      return (res << 8) + readU1
    }


    def readU4: Int = {
      var res: Int = readU2
      return (res << 16) + readU2
    }


    def writeU1(value: Int): Unit = {
      gen(({genPos += 1; genPos})) = value.asInstanceOf[Byte]
    }


    def writeU2(value: Int): Unit = {
      writeU1(value >> 8)
      writeU1(value & 0xFF)
    }


    def writeU4(value: Int): Unit = {
      writeU2(value >> 16)
      writeU2(value & 0xFFFF)
    }

    def copy(count: Int): Unit = {
      var i: Int = 0
      while (i < count) {
        writeU1(readU1)
        i += 1
      }
    }


    def readBytes(count: Int): Array[Byte] = {
      var bytes: Array[Byte] = new Array[Byte](count)
      var i: Int = 0
      while (i < count) {
        bytes(i) = orig(({origPos += 1; origPos}))
        i += 1
      }
      return bytes
    }

    def writeBytes(bytes: Array[Byte]): Unit = {
      var i: Int = 0
      while (i < bytes.length) {
        gen(({genPos += 1; genPos})) = bytes(i)
        i += 1
      }
    }

    def copyConstantPool(constantPoolCount: Int): Int = {
      var sdeIndex: Int = -1
      var i: Int = 1
      while (i < constantPoolCount) {
        var tag: Int = readU1
        writeU1(tag)
        tag match {
          case 8 | 7 =>
            copy(2)
          case 9 | 10 | 11 | 3 | 4 | 12 =>
            copy(4)
          case 5 | 6 =>
            copy(8)
            ({i += 1; i})
          case 1 =>
            var len: Int = readU2
            writeU2(len)
            var utf8: Array[Byte] = readBytes(len)
            var str: String = new String(utf8, "UTF-8")
            if (str.equals(nameSDE)) {
              sdeIndex = i
            }
            writeBytes(utf8)
          case _ =>
            throw new IOException("unexpected tag: " + tag)
        }
        i += 1
      }
      return sdeIndex
    }


    def writeUtf8ForSDE: Unit = {
      var len: Int = nameSDE.length
      writeU1(1)
      writeU2(len)
      var i: Int = 0
      while (i < len) {
        writeU1(nameSDE.charAt(i))
        i += 1
      }
    }

  }

  val nameSDE = "SourceDebugExtension";

  def apply(classFile: File, sourceDebug: File): Unit = {
    apply(classFile, read(sourceDebug))
  }

  def apply(classFile: File, sourceDebug: Array[Byte]): Unit = {
    var tmpFile = new File(classFile.getPath() + "tmp");
    apply(classFile, sourceDebug, tmpFile);
    if (!classFile.delete()) {
      throw new IOException("temp file delete failed");
    }
    if (!tmpFile.renameTo(classFile)) {
      throw new IOException("temp file rename failed");
    }
  }

  def apply(input: File, sourceDebug: Array[Byte], output: File): Unit = {
    apply(read(input), sourceDebug, output)
  }

  def apply(input: File, sourceDebug: File, output: File): Unit = {
    apply(read(input), read(sourceDebug), output)
  }

  def apply(input: Array[Byte], sourceDebug: Array[Byte], output: File): Unit = {
    IOUtil.writeBinaryFile(output, apply(input, sourceDebug))
  }

  def apply(input: Array[Byte], sourceDebug: Array[Byte]): Array[Byte] = {
    (new Installer(input, sourceDebug)).install
  }

  private def read(input: File) = {
    if (!input.exists()) {
      throw new FileNotFoundException("no such file: " + input);
    }
    IOUtil.loadBinaryFile(input)
  }

}

