package org.fusesource.scalate
package util

import Measurements._

class UnitFormatterTest extends FunSuiteSupport {

  test("bytes output") {
    assertResult("0 bytes") {
      byte(0)
    }
    assertResult("1 byte") {
      byte(1)
    }
    assertResult("1 byte") {
      byte(1.2)
    }
    assertResult("42 bytes") {
      byte(42)
    }
    assertResult("12 K") {
      byte(12 * 1024)
    }
    assertResult("5 Mb") {
      byte(5 * 1024 * 1024)
    }
    assertResult("6 Gb") {
      byte(6L * 1024 * 1024 * 1024)
    }
  }

  test("millis output") {
    assertResult("0 millis") {
      milli(0)
    }
    assertResult("1 milli") {
      milli(1.2)
    }
    assertResult("1 second") {
      milli(1000)
    }
    assertResult("30 seconds") {
      milli(30 * 1000)
    }
    assertResult("10 minutes") {
      milli(10 * 60 * 1000)
    }
  }

  test("seconds output") {
    assertResult("0 seconds") {
      second(0)
    }
    assertResult("1 second") {
      second(1.2)
    }
    assertResult("30 seconds") {
      second(30)
    }
    assertResult("1 minute") {
      second(60)
    }
    assertResult("20 hours") {
      second(20 * 60 * 60)
    }
    assertResult("2 days") {
      second(2 * 24 * 60 * 60)
    }
  }

  test("using explicit units directly") {
    assertResult("42 K") {
      k(42)
    }
    assertResult("12 Mb") {
      k(12 * 1024)
    }
    assertResult("5 Gb") {
      k(5 * 1024 * 1024)
    }
  }

}
