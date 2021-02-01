package org.fusesource.scalate.support

import org.fusesource.scalate.parsers.TemplateException

/**
 * Indicates a Scala compiler error occurred when converting the template into bytecode
 */
class CompilerException(
  msg: String,
  val errors: List[CompilerError]) extends TemplateException(msg)
