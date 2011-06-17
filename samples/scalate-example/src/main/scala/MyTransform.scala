import org.fusesource.scalate.scuery._

case class Person(name: String, location: String)


object MyTransform extends Transformer {
  val people = List(Person("James", "Beckington"), Person("Hiram", "Tampa"))
  
  $(".people").contents {
    node =>
      people.flatMap {
        p =>
          transform(node.$("li:first-child")) {
            $ =>
              $("a.person").contents = p.name
              $("a.person").attribute("href").value = "http://acme.com/bookstore/" + p.name
          }
      }
  }
}