package org.fusesource.scalate.util

object Measurements {
  val log = Log(classOf[UnitOfMeasure])

  // computer memory/disk sizes
  val gb = UnitOfMeasure("Gb", "Gb")
  val mb = UnitOfMeasure("Mb", "Mb", gb, 1024)
  val k = UnitOfMeasure("K", "K", mb, 1024)
  val byte = UnitOfMeasure("bytes", "byte", k, 1024)


  // times
  val millenium = UnitOfMeasure("milleniums", "millenium")
  val century = UnitOfMeasure("centuries", "century", millenium, 10)
  val decade = UnitOfMeasure("decades", "decade", century, 10)
  val year = UnitOfMeasure("years", "year", decade, 10)
  val week = UnitOfMeasure("weeks", "week", year, 52)
  val day = UnitOfMeasure("days", "day", week, 7)
  val hour = UnitOfMeasure("hours", "hour", day, 24)
  val minute = UnitOfMeasure("minutes", "minute", hour, 60)
  val second = UnitOfMeasure("seconds", "second", minute, 60)
  val milli = UnitOfMeasure("millis", "milli", second, 1000)

  // simple amounts
  val billion = UnitOfMeasure("B", "K")
  val million = UnitOfMeasure("M", "M", billion, 1000)
  val thousand = UnitOfMeasure("K", "K", million, 1000)
  val amount = UnitOfMeasure("", "", thousand, 1000)
}

import Measurements.log


case class UnitOfMeasure(unitsName: String, unitName: String, parent: UnitOfMeasure = null, size: Double = 0) {
  // we are using null rather than None as it seems a bit easier on the DSL defining the data
  if (parent != null) {
    assert(size != 0, "Unit should never have size of zero if we have a parent!")
  }

  def apply(number: Any, defaultExpression: String = ""): String = number match {
    case n: Number => apply(n.doubleValue, defaultExpression)
    case null => defaultExpression
    case a: AnyRef =>
      val text = a.toString
      try {
        apply(text.toDouble, defaultExpression)
      } catch {
        case e => log.debug("Could not convert " + text + " to a number: " + e, e)
        defaultExpression
      }
    case _ => defaultExpression
  }

  def apply(number: Double, defaultExpression: String): String = {
    if (parent != null && number >= size) {
      parent.apply(number / size, defaultExpression)
    } else {
      toString(number, defaultExpression)
    }
  }

  def toString(d: Double, defaultExpression: String = ""): String = {
    val n = d.round
    val postfix = if (n == 1L) unitName else unitsName
    "" + n + " " + postfix
  }
}