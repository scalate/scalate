package org.fusesource.scalate.wikitext

/**
 * A confluence tag for a generic expression
 */
case class ExpressionTag(
  tag: String,
  fn: () => AnyRef) extends AbstractConfluenceTagSupport(tag) {

  def setOption(key: String, value: String) =
    Blocks.unknownAttribute(key, value)

  def doTag() = {
    val result = fn()
    val text = if (result == null) "" else result.toString
    builder.characters(text)
  }
}
