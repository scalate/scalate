package org.fusesource.scalate

import servlet.ServletTemplateEngine

object MockBootstrap {
  var initialised = false
}

class BootTest extends TemplateTestSupport {

  test("scalate.Boot gets invoked") {
    // lets pretend to be a web app
    ServletTemplateEngine.runBoot()

    //assertOutputContains(TemplateSource.fromText("foo/something.ssp", "hello world!"), "hello world!")

    expect(true, "scalate.Boot not invoked!") { MockBootstrap.initialised }
  }
}