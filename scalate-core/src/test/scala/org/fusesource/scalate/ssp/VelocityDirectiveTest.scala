package org.fusesource.scalate.ssp

import org.fusesource.scalate.TemplateTestSupport

/**
 * @version $Revision : 1.1 $
 */
class VelocityDirectiveTest extends TemplateTestSupport {

  test("for loop") {
    assertOutput("xxx", "#for(i <- 1 to 3)x#end")
  }


}