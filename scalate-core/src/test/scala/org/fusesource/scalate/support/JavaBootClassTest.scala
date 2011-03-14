package org.fusesource.scalate
package support

class JavaBootClassTest extends TemplateTestSupport {
  test("Java Boot class") {
    Boots.invokeBoot(classOf[MyJavaBoot], List(engine))

    expect(1) { MyJavaBoot.runCount }
  }
}