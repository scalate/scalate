package org.fusesource.scalate.scaml

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.fusesource.scalate._
import java.io.{StringWriter, PrintWriter, File}
/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@RunWith(classOf[JUnitRunner])
class ScamlTestSupport extends FunSuite {

  var engine = new TemplateEngine

  def testRender(description:String, template:String, result:String) = {
    test(description) {
      expect(result) { render(template) }
    }
  }

  def ignoreRender(description:String, template:String, result:String) = {
    ignore(description) {
      expect(result) { render(template) }
    }
  }

  def render(content:String): String = {

    engine.resourceLoader = new FileResourceLoader {
      override def load( uri: String ): String = content
      override def lastModified(uri:String): Long = 0
    }

    val buffer = new StringWriter()
    val out = new PrintWriter(buffer)
    val context = new DefaultRenderContext(engine, out) {
      val name = "Hiram"
      val title = "MyPage"
      val href = "http://scalate.fusesource.org"
      val quality = "scrumptious"
    }

    engine.bindings = List(Binding("context", context.getClass.getName, true))

    context.attributes("context") = context
    context.attributes("bean") = Bean("red", 10)

    val template = engine.compile("/org/fusesource/scalate/scaml/test.scaml")
    template.render(context)
    out.close
    buffer.toString
  }
}