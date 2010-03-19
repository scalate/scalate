package org.fusesource.scalate.scaml


import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

/**
 * Some tests for Scaml bugs reported
 */
@RunWith(classOf[JUnitRunner])
class ScamlBugTest extends ScamlTestSupport {

    testRender("SCALATE-45 test1",
"""
- if (name == "Hiram")
  - if (title == "MyPage")
    Worked!

  - else
    Failed

- else
  Failed
""","""
Worked
""")
}