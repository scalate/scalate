package org.fusesource.scalate.converter

import org.fusesource.scalate.support.Text
import org.fusesource.scalate.util.Logging

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

class JspConverter extends IndentWriter with Logging {
  var coreLibraryPrefix: String = "c"
  var whenCount = 0
      
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
          case "choose" =>
            whenCount = 0
            convert(body)
            print("#end")

          case "forEach" =>
            val varExp = e.attributeMap.getOrElse("var", textExpression("i"))
            print("#for(" + asUnquotedParam(varExp) + " <- ")

            e.attributeMap.get("items") match {
              case Some(exp) =>
                print(asParam(exp) + ")")

              case _ =>
                val begin = e.attribute("begin")
                val end = e.attribute("end")
                print(asUnquotedParam(begin) + ".to(" + asUnquotedParam(end))

                e.attributeMap.get("step") match {
                  case Some(step) => print(", " + asUnquotedParam(step))
                  case _ =>
                }
                print("))")
            }
            convert(body)
            print("#end")

          case "if" =>
            val exp = e.attribute("test")
            print("#if(" + asParam(exp) + ")")
            convert(body)
            print("#end")

          case "otherwise" =>
            print("#else")
            convert(body)

          case "out" =>
            val exp = e.attribute("value")
            print("${" + asParam(exp) + "}")

          case "set" =>
            val exp = e.attribute("value")
            val name = e.attribute("var")
            print("#{ var " + asUnquotedParam(name) + " = " + asParam(exp) + " }#")

          case "when" =>
            val exp = e.attribute("test")
            print("#" + (if (whenCount == 0) "if" else "elseif") + "(" + asParam(exp) + ")")
            whenCount +=1
            convert(body)

          case "url" =>
            val exp = e.attribute("value")
            print("${uri(" + asParam(exp) + ")}")

          case _ =>
            warn("No converter available for tag <" + coreLibraryPrefix + ":" + name + ">: " + e)
            print(e)
        }
      case _ => print(e)
    }
  }

  def print(e: Element): Unit = {
    print("<" + e.qualifiedName)
    for (a <- e.attributes) {
      print(" " + a.name + "=\"" + asParam(a.value) + "\"")
    }
    print("/>")
  }

  protected def textExpression(s: String) = TextExpression(Text(s))

  /**
   * Returns the text of an expression as a numeric method parameter
   */
  protected def asUnquotedParam(exp: Expression): String = exp match {
    case t: TextExpression => t.text.toString
    case d: DollarExpression => d.code.toString
    case CompositeExpression(list) => list.map(asUnquotedParam(_)).mkString(" + ")
  }

  /**
   * Returns the text of an expression as a method parameter
   */
  protected def asParam(exp: Expression): String = exp match {
    case t: TextExpression => "\"" + t.text + "\""
    case d: DollarExpression => d.code.toString
    case CompositeExpression(list) => list.map(asParam(_)).mkString(" + ")
  }

  /**
   * Returns the text of an expression as a method parameter
   */
  protected def asJsp(exp: Expression): String = exp match {
    case t: TextExpression => t.text.toString
    case d: DollarExpression => "${" + d.code + "}"
    case CompositeExpression(list) => list.map(asJsp(_)).mkString("")
  }

}