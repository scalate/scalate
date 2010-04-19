package org.fusesource.scalate.squery

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

    val result = transformer.transform(xml)

    println("got result: " + result)

    val a = (result \\ "a")(0)
    expect("http://scalate.fusesource.org/") {a \ "@href"}
    expect("foo") {a \ "@class"}
    expect("A link") {a \ "@title"}
  }
}