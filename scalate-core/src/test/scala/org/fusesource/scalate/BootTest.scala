package org.fusesource.scalate

import servlet.ServletTemplateEngine
import org.fusesource.scalate.util.ClassLoaders

object MockBootstrap {
  var initialised = false
}

class BootTest extends TemplateTestSupport {

  test("scalate.Boot gets invoked") {

    val engine = new TemplateEngine()
    engine.boot

    //assertOutputContains(TemplateSource.fromText("foo/something.ssp", "hello world!"), "hello world!")

    expect(true, "scalate.Boot not invoked!") { MockBootstrap.initialised }
  }
}