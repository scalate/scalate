package org.fusesource.scalate.ssp

import org.fusesource.scalate.{InvalidSyntaxException, TemplateTestSupport}

/**
 * @version $Revision : 1.1 $
 */
class VelocityDirectiveTest extends TemplateTestSupport {

  test("for loop") {
    assertOutput("xxx", "#for(i <- 1 to 3)x#end")
  }

  test("missing #end") {
    val e = intercept[InvalidSyntaxException] {
      assertOutput("xxx", "#for(i <- 1 to 3) blah")
    }
    //println("caught: " + e)
  }

  test("too many #end") {
    val e = intercept[InvalidSyntaxException] {
      assertOutput("xxx", "#for(i <- 1 to 3) blah #end #end")
    }
    //println("caught: " + e)
  }

}