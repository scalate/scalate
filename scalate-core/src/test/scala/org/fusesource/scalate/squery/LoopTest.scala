package org.fusesource.scalate.squery

// Note this import is required so the implicit conversion Transform.toNodes works
import _root_.org.fusesource.scalate.squery.Transform._

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
          }.apply(node)
        }                                   
      }
    }
    assertTransformed(transformer)
  }

  test("loop using Transform statement on each person") {
    object transformer extends Transformer {
      $(".person") { node =>

        // Note you must import Transform._ to be able to
        // miss out the Transform.toNodes method call

        people.flatMap { p =>
          new Transform(node) {
            $(".name").contents = p.name
            $(".location").contents = p.location
          }
        }
      }
    }
    assertTransformed(transformer)
  }

  test("loop using transform method on each person") {
    object transformer extends Transformer {
      $(".person") { node =>
        people.flatMap { p =>
          transform(node) { $ =>
            $(".name").contents = p.name
            $(".location").contents = p.location
          }
        }
      }
    }
    assertTransformed(transformer)
  }

  def assertTransformed(transformer: Transformer): Unit = {
    val result = transformer(xml)

    println("got result: " + result)

    expect("James") {(result \\ "td")(0).text}
    expect("Beckington") {(result \\ "td")(1).text}

    expect("Hiram") {(result \\ "td")(2).text}
    expect("Tampa") {(result \\ "td")(3).text}
  }
}