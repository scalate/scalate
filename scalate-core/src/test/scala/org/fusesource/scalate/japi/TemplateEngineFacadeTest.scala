package org.fusesource.scalate
package japi

import demo.UseScalateFromJava

class TemplateEngineFacadeTest extends FunSuiteSupport {
  test("using TemplateEngine from Java") {
    UseScalateFromJava.main(Array("org/fusesource/scalate/demo/sample.jade"))
  }
}
