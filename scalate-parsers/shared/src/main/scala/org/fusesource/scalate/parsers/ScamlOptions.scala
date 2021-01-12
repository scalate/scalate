package org.fusesource.scalate.parsers

/**
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object ScamlOptions {

  object Format extends Enumeration {
    val xhtml, html4, html5 = Value
  }

  val DEFAULT_FORMAT = Format.xhtml
  val DEFAULT_AUTOCLOSE = List(
    "meta",
    "img",
    "link",
    "br",
    "hr",
    "input")

  val DEFAULT_INDENT = "  "
  val DEFAULT_NL = "\n"
  val DEFAULT_UGLY = false

  var format = DEFAULT_FORMAT

  /**
   * If this is set to null, then all elements is be autoclosed.
   */
  var autoclose = DEFAULT_AUTOCLOSE

  /**
   * The indent type used in Scaml markup output.  Defaults
   * to two spaces.
   */
  var indent = DEFAULT_INDENT

  /**
   * The newline separator used in the produced markup output.  Defaults
   * to <code>"\n"</code>.
   */
  var nl = DEFAULT_NL

  /**
   * Use ugly content rendering by default.  When ugly rendering is
   * enabled, evaluated content is not re-indented and newline
   * preservation is not applied either.
   */
  var ugly = DEFAULT_UGLY

}
