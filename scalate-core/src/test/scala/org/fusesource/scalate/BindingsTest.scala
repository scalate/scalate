package org.fusesource.scalate

class BindingsTest extends TemplateTestSupport {
  test("can use a template with default binding expression") {
    val responseClassName = classOf[DummyResponse].getName
    engine.bindings = List(
    Binding("context", classOf[DefaultRenderContext].getName, true, isImplicit = true),
    Binding("response", responseClassName, defaultValue = Some("new " + responseClassName + "()")))

    val text = engine.layout(TemplateSource.fromText("foo.ssp","hello ${response}"))

    println("Got: " + text)
  }
}


class DummyResponse {
  def setContentType(value: String): Unit = {}
}