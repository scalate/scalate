package org.fusesource.scalate.mustache


import org.fusesource.scalate._
import support.{Code, AbstractCodeGenerator}
import collection.mutable.Stack

/**
 * @version $Revision: 1.1 $
 */
class MustacheCodeGenerator extends AbstractCodeGenerator[Statement] {
  override val stratumName = "MSC"

  implicit def textToString(text: Text) = text.value

  implicit def textOptionToString(text: Option[Text]): Option[String] = text match {
    case None => None
    case Some(x) => Some(x.value)
  }

  private class SourceBuilder extends AbstractSourceBuilder[Statement] {
    protected val scopes = new Stack[String]
    protected var scopeIndex = 1

    protected def isImportStatementOrCommentOrWhitespace(fragment: Statement) = fragment match {
      //case s: Text if (s.value.trim.length == 0) => true
      case s: Comment => true
      case _ => false
    }

    def generate(fragments: List[Statement]): Unit = {
      this << "import _root_.org.fusesource.scalate.mustache._"
      this << ""
      
      addScope("RenderContextScope")
      fragments.foreach(generate)
    }

    def generate(fragment: Statement): Unit = {
      fragment match {
        case Comment(code) => {
        }
        case Text(text) => {
          this << fragment.pos;
          this << "$_scalate_$_context << ( " + asString(text) + " )"
        }
        case Variable(name, unescape) => {
          this << fragment.pos;
          this << "" + scopes.head + ".renderVariable($_scalate_$_context, \"" + name + "\", " + unescape + ")"
        }

        case s => {
          println("Unsupported: " + s)
        }
      }
    }

    protected def addScope(expression: String): String = {
      val name = "$_scope_" + scopeIndex
      scopeIndex += 1
      scopes.push(name)
      this << "val " + name + " = " + expression
      name
    }
  }

  override def generate(engine: TemplateEngine, source: TemplateSource, bindings: List[Binding]): Code = {

    // Load the translation unit
    val content = source.text
    val uri = source.uri

    // Determine the package and class name to use for the generated class
    val (packageName, className) = extractPackageAndClassNames(uri)

    // Parse the translation unit
    val fragments = (new MustacheParser).parse(content)

    val sb = new SourceBuilder
    sb.generate(engine, packageName, className, bindings, fragments)

    Code(this.className(uri), sb.code, Set(uri), sb.positions)
  }
}

