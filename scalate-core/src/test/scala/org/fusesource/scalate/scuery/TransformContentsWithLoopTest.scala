package org.fusesource.scalate.scuery

import _root_.org.fusesource.scalate.FunSuiteSupport
import _root_.scala.xml.Node

class TransformContentsWithLoopTest extends FunSuiteSupport {
  val people = List(Person("James", "Beckington"), Person("Hiram", "Tampa"))

  val xml = <ul class="people">
    <li>
      <a href="#" class="person">A person</a>
    </li>
    <li>
      <a href="#" class="person">Another person</a>
    </li>
  </ul>


  test("transform contents") {

    object transformer extends Transformer {
      $(".people").contents {
        node =>
        // TODO replace with a simple li:first selector!
          val li = (node \ "li")(0)

          people.flatMap {
            p =>
              transform(li) {
                $ =>
                  $("a.person").contents = p.name
                  $("a.person").attribute("href").value = "http://acme.com/bookstore/" + p.name
              }
          }
      }
    }

    val result = transformer(xml)
    println("got result: " + result)

    assertPersonLink((result \ "li" \ "a")(0), "James")
    assertPersonLink((result \ "li" \ "a")(1), "Hiram")
  }

  protected def assertPersonLink(a: Node, name: String): Unit = {
    println("Testing " + a + " for name: " + name)
    expect(name) {a.text}
    expect("http://acme.com/bookstore/" + name) {a \ "@href"}
  }
}