package org.fusesource.scalate.resource

case class StringResource(uri: String, override val text: String) extends TextResource
