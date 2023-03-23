package org.fusesource.scalate.mustache

sealed trait VariableResult {
  def toOption: Option[Any]

  def flatMap(f: Any => VariableResult): VariableResult

  def noVariableAsNoValue: VariableResult
}

object VariableResult {
  case class SomeValue(value: Any) extends VariableResult {
    def toOption: Option[Any] = Some(value)

    def flatMap(f: Any => VariableResult): VariableResult = f(value)

    def noVariableAsNoValue: VariableResult = this
  }

  case object NoValue extends VariableResult {
    def toOption: Option[Any] = None

    def flatMap(f: Any => VariableResult): VariableResult = this

    def noVariableAsNoValue: VariableResult = this
  }

  case object NoVariable extends VariableResult {
    def toOption: Option[Any] = None

    def flatMap(f: Any => VariableResult): VariableResult = this

    def noVariableAsNoValue: VariableResult = NoValue
  }

  def apply(maybeValue: Option[Any], variableExist: => Boolean): VariableResult = {
    maybeValue.map(SomeValue.apply).getOrElse {
      if (variableExist) NoValue else NoVariable
    }
  }
}