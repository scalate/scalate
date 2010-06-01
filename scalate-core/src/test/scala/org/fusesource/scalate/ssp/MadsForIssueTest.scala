package org.fusesource.scalate.ssp

import org.fusesource.scalate.{TemplateTestSupport}

/**
 * @version $Revision : 1.1 $
 */
class MadsForIssueTest extends TemplateTestSupport {

  test("issue with #for") {
    val template = compileSsp("using #for", """<%@ var fields:List[String] %>
start
<% fields.foreach{ field => %>
f1 ${field}
<% } %>

#for( field <- fields)
f2 ${field}
#end
done
""")

    assertTrimOutput("""start
f1 a
f1 b


f2 a

f2 b

done""", template, Map("fields" -> List("a", "b")))
  }


  test("mads sample") {
    val attributes = Map("name" -> "Cheese",
        "thePackage" -> "org.acme.cheese",
        "fields" -> List("foo", "bar"))

    val template = compileSsp("Mads sample", """
<%@ var name:String %>
<%@ var thePackage:String %>
<%@ var fields:List[String] %>

package ${thePackage}

import net.liftweb._
import mapper._
import http._
import SHtml._
import util._

class ${name} extends LongKeyedMapper[${name}] with IdPK {

       def getSingleton = ${name}

<% fields.foreach{ field => %>
       object ${field} extends MappedField(this)
<% } %>

#for(field <- fields)
       object ${field} extends MappedField(this)
#end

}
object ${name} extends ${name} with LongKeyedMetaMapper[${name}]    
""")

    val output = engine.layout(template, attributes)
    debug("Output:")
    debug(output)
  }

}