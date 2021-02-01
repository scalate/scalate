package org.fusesource.scalate.util

import slogging.LazyLogging

import java.io._

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

  object Writer
  class Writer(val orig: Array[Byte], val sourceDebug: String) extends LazyLogging {
    import logger._

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
        val tag: Int = dis.readByte
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
      sdeIndex
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
      sdeFound
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
      rc
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
        i += 1
      }
      rc
    }
  }

  val nameSDE = "SourceDebugExtension"

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
    val tmpFile = new File(classFile.getPath() + "tmp")
    store(classFile, sourceDebug, tmpFile)
    if (!classFile.delete()) {
      throw new IOException("temp file delete failed")
    }
    if (!tmpFile.renameTo(classFile)) {
      throw new IOException("temp file rename failed")
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
      throw new FileNotFoundException("no such file: " + input)
    }
    IOUtil.loadBinaryFile(input)
  }

  def main(args: Array[String]) = {
    val smap1 = new SourceMap()
    smap1.setOutputFileName("foo.scala")
    val straturm = new SourceMapStratum("JSP")
    straturm.addFile("foo.scala", "path/to/foo.scala")
    straturm.addLine(1, 0, 1, 2, 1)
    straturm.addLine(2, 0, 1, 3, 1)
    straturm.addLine(4, 0, 1, 8, 1)
    straturm.addLine(5, 0, 1, 9, 1)
    smap1.addStratum(straturm, true)
    val text1 = smap1.toString
    println(text1)

    val smap2 = SourceMap.parse(text1)
    val text2 = smap2.toString
    println(text2)

    println(text2 == text1)

    println(smap2.mapToStratum(3))

  }
}
