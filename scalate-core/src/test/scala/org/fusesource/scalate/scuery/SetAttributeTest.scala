package org.fusesource.scalate.scuery

import _root_.org.fusesource.scalate.FunSuiteSupport
import xml.Node

class SetAttributeTest extends FunSuiteSupport {
  val xml = <html>
    <body>
      <div id="content">
        <a href="#" class="foo" title="A foo link">foo link</a>
        <a href="#" class="bar" title="A bar link">bar link</a>
        <a href="#" class="jog" title="A jog link">jog link</a>
      </div>
    </body>
  </html>



  test(" transform") {
    object transformer extends Transformer {

      // 3 different approaches to changing attributes
      $("a.foo").attribute("href", "http://scalate.fusesource.org/")

      $("a.bar").attribute("href").value = "http://scalate.fusesource.org/documentation/"


      $("a.jog").attribute("href") {
        e =>
          "http://scalate.fusesource.org/documentation/" + (e \ "@class") + ".html"
      }
    }

    val result = transformer(xml)

    println("got result: " + result)

    assertLink((result \\ "a")(0), "http://scalate.fusesource.org/", "foo", "A foo link")
    assertLink((result \\ "a")(1), "http://scalate.fusesource.org/documentation/", "bar", "A bar link")
    assertLink((result \\ "a")(2), "http://scalate.fusesource.org/documentation/jog.html", "jog", "A jog link")
  }

  def assertLink(a: Node, href: String, className: String, title: String): Unit = {
    println("testing link node: " + a)
    expect(href) {a \ "@href"}
    expect(className) {a \ "@class"}
    expect(title) {a \ "@title"}
  }
}