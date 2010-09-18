package sample

trait SomeTrait {
  def someValue = "This is someValue from a trait"
  def anotherValue = "This is someValue from a trait"
}

case class SomeClass() extends SomeTrait {
  override def anotherValue = "This is anotherValue from a class"
}