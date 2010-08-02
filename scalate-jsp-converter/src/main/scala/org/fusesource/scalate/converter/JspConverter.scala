package org.fusesource.scalate.converter

trait IndentWriter {
  var out = new StringBuilder
  var indentLevel: Int = 0
  var indentText: String = "  "

  def reset: Unit = {
    out = new StringBuilder
  }

  def indent[T](op: => T): T = {indentLevel += 1; val rc = op; indentLevel -= 1; rc}

  def println(line: String): this.type = {
    for (i <- 0 until indentLevel) {
      print(indentText)
    }
    print(line)
    println
    this
  }

  def print(value: AnyRef): Unit = {
    out.append(value)
  }

  def println = print("\n")


  def text = out.toString
}

class JspConverter extends IndentWriter {
  var coreLibraryPrefix: String = "c"

  def convert(jsp: String): String = {
    reset

    val parser = new JspParser
    val result = parser.parsePage(jsp)
    convert(result)
    text
  }

  def convert(list: List[PageFragment]): Unit = {
    for (r <- list) {
      convert(r)
    }
  }

  def convert(fragment: PageFragment): Unit = fragment match {
    case e: Element => transform(e)
    case _ => print(fragment.toString)
  }

  def transform(e: Element): Unit = {
    e match {

      // core JSTL library
      case Element(QualifiedName(coreLibraryPrefix, name), attributes, body) =>
        name match {
          case "if" =>
            val exp = e.attributeMap.getOrElse("test", "true")
            print("#if(" + exp + ")")
            convert(body)
            print("#end")

          case "url" =>
            val exp = e.attributeMap.getOrElse("value", "")
            print("${uri(\"" + exp + "\")}")

          case _ => print(e)
        }
      case _ => print(e)
    }
  }

  def print(e: Element): Unit = {
    print("<" + e.qualifiedName)
    for (a <- e.attributes) {
      print(" " + a.name + "=\"" + a.value + "\"")
    }
    print("/>")
  }
}