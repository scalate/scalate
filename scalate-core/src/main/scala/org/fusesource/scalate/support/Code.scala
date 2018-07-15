package org.fusesource.scalate.support

import _root_.scala.util.parsing.input.OffsetPosition
import scala.collection.immutable.TreeMap

case class Code(
  className: String,
  source: String,
  dependencies: Set[String],
  positions: TreeMap[OffsetPosition, OffsetPosition])
