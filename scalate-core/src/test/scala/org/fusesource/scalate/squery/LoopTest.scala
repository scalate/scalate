package org.fusesource.scalate.squery

import _root_.org.fusesource.scalate.FunSuiteSupport
case class Person(name: String, location: String)

class LoopTest extends FunSuiteSupport {
  val people = List(Person("James", "Beckington"), Person("Hiram", "Tampa"))

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
          <tr class="person">
            <td class="name"></td>
            <td class="location"></td>
          </tr>
        </table>
      </div>
      <div id="messages"></div>
      <div id="footer">Footer</div>
    </body>
  </html>


  test("loop using new transformer on each person") {
    object transformer extends Transformer {
      $(".person") { node =>

        people.flatMap { p =>
          new Transformer {
            $(".name").contents = p.name
            $(".location").contents = p.location
          }.transform(node)
        }
      }
    }
    assertTransformed(transformer)
  }

  test("loop using Transform statement on each person") {
    object transformer extends Transformer {
      $(".person") { node =>

        people.flatMap { p =>
          new Transform(node) {
            $(".name").contents = p.name
            $(".location").contents = p.location
          }.toNodes
        }
      }
    }
    assertTransformed(transformer)
  }

  def assertTransformed(transformer: Transformer): Unit = {
    val result = transformer.transform(xml)

    println("got result: " + result)

    expect("James") {(result \\ "td")(0).text}
    expect("Beckington") {(result \\ "td")(1).text}

    expect("Hiram") {(result \\ "td")(2).text}
    expect("Tampa") {(result \\ "td")(3).text}
  }
}