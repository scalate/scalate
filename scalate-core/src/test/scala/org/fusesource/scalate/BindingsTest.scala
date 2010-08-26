package org.fusesource.scalate

class BindingsTest extends TemplateTestSupport {
  test("can use a template with default binding expression") {
    val responseClassName = classOf[DummyResponse].getName
    engine.bindings = List(
      Binding("context", classOf[DefaultRenderContext].getName, true, isImplicit = true),
      Binding("response", responseClassName, defaultValue = Some("new " + responseClassName + "()")))

    val text = engine.layout(TemplateSource.fromText("foo.ssp", "hello ${response}"))

    info("Got: " + text)
  }

  test("Int binding with a default value") {
    engine.bindings = List(Binding("year", "Int", false, Some("1970")))

    val text1 = engine.layout(TemplateSource.fromText("foo2.ssp", "${year.toString}'s is the hippies era"))
    info("Got: " + text1)
    expect("1970's is the hippies era") {text1.trim}

    val text2 = engine.layout(TemplateSource.fromText("foo3.ssp", "${year.toString}'s is the hippies era"), Map("year" -> 1950))
    info("Got: " + text2)
    expect("1950's is the hippies era") {text2.trim}
  }
}


class DummyResponse {
  def setContentType(value: String): Unit = {}
}