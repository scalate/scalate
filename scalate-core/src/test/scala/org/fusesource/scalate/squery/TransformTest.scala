package org.fusesource.scalate.squery

import _root_.org.fusesource.scalate.FunSuiteSupport
import xml.Text

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
      <div id="messages"></div>
      <div id="footer">Footer</div>
    </body>
  </html>

  test("try simple transform") {

    object transformer extends Transformer {

      $("table .name").contents = "Hiram"
      $(".location").contents = <b>Tampa</b>
    }

    val result = transformer(xml)
    println("got result: " + result)

    expect("Hiram") { (result \\ "td")(0).text }
    expect("Tampa") { (result \\ "td" \\ "b")(0).text }
  }

}