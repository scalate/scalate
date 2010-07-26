/**
 * Copyright (C) 2009-2010 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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