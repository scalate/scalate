package org.fusesource.scalate.squery

import _root_.org.fusesource.scalate.FunSuiteSupport
import xml.Text

object PersonTransform extends Transformer {

  $("table .name").content = "Hiram"
  $(".location").content = <b>Tampa</b>
}

class TransformTest extends FunSuiteSupport {
  val xml = <html>
    <head>
      <title>My Title</title>
    </head>
    <body>
      <div id="header">Header</div>
      <div id="content">
        <table class="people">
          <tr>
            <th>Name</th>
            <th>Location</th>
          </tr>
          <tr>
            <td class="name">James</td>
            <td class="location">Beckington</td>
          </tr>
        </table>
      </div>
      <div id="footer">Footer</div>
    </body>
  </html>

  val transformer: Transformer = PersonTransform

  test("try simple transform") {
    val result = transformer.transform(xml)

    println("got result: " + result)

    expect("Hiram") { (result \\ "td")(0).text }
    expect("Tampa") { (result \\ "td" \\ "b")(0).text }
  }

  test("selector"){
    val e = <td class="name">hey</td>

    val s = Selector(".name")

    expect(true) { s.matches(e, Nil) }
  }

}