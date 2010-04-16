package org.fusesource.scalate.support

import scala.util.parsing.input.{NoPosition, Position}

case class CompilerError(file: String, message: String, pos: Position = NoPosition, original: CompilerError = null) {
}

