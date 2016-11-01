package org.fusesource.scalate
package scuery

class WilleScueryTest extends FunSuiteSupport {

  var verbose = false

  test("hello") {
    val people = List(Person("James", "Mells"), Person("Hiram", "Tampa"))

    object transformer extends Transformer {
      $(".person") { node =>
        people.flatMap {
          p =>
            new Transform(node) {
              $(".name").contents = p.name
              $(".location").contents = p.location
            }
        }
      }
    }

    val result = transformer(xml)
    show("got result: " + result)

    val names = result.$(".name")
    show("CSS filter got: " + names + " of size: " + names.size)

    expect("James") {
      names(0).text
    }
    expect("Hiram") {
      names(1).text
    }
  }

  def show(m: => String): Unit = {
    if (verbose) {
      info(m)
    } else {
      debug(m)
    }
  }

  def xml = <div id="content">
              <table class="people">
                <tr>
                  <th>Name</th>
                  <th>Location</th>
                </tr>
                <tr class="person">
                  <td class="name">DummyName</td>
                  <td class="location">DummyLocation</td>
                </tr>
              </table>
            </div>
}

