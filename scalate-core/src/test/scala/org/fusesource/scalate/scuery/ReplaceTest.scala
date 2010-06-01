package org.fusesource.scalate.scuery

import _root_.org.fusesource.scalate.FunSuiteSupport

class ReplaceTest extends FunSuiteSupport {
  val xml = <html>
    <body>
      <div id="content">
        <a href="#" class="foo" title="A link">Some Link</a>
      </div>
    </body>
  </html>



  test(" transform") {
    object transformer extends Transformer {
      $("a.foo") {
        n =>
          <a href="http://scalate.fusesource.org/" class={n \ "@class"} title={n \ "@title"}>
            {n.text}
          </a>
      }
    }

    val result = transformer(xml)

    debug("got result: " + result)

    val a = (result \\ "a")(0)
    expect("http://scalate.fusesource.org/") {(a \ "@href").toString}
    expect("foo") {(a \ "@class").toString}
    expect("A link") {(a \ "@title").toString}
  }
}