package org.fusesource.scalate
package util

import Measurements._

class UnitFormatterTest extends FunSuiteSupport {

  test("bytes output") {
    expect("0 bytes") {
      byte(0)
    }
    expect("1 byte") {
      byte(1)
    }
    expect("1 byte") {
      byte(1.2)
    }
    expect("42 bytes") {
      byte(42)
    }
    expect("12 K") {
      byte(12 * 1024)
    }
    expect("5 Mb") {
      byte(5 * 1024 * 1024)
    }
    expect("6 Gb") {
      byte(6L * 1024 * 1024 * 1024)
    }
  }

  test("millis output") {
    expect("0 millis") {
      milli(0)
    }
    expect("1 milli") {
      milli(1.2)
    }
    expect("1 second") {
      milli(1000)
    }
    expect("30 seconds") {
      milli(30 * 1000)
    }
    expect("10 minutes") {
      milli(10 * 60 * 1000)
    }
  }

  test("seconds output") {
    expect("0 seconds") {
      second(0)
    }
    expect("1 second") {
      second(1.2)
    }
    expect("30 seconds") {
      second(30)
    }
    expect("1 minute") {
      second(60)
    }
    expect("20 hours") {
      second(20 * 60 * 60)
    }
    expect("2 days") {
      second(2 * 24 * 60 * 60)
    }
  }

  test("using explicit units directly") {
    expect("42 K") {
      k(42)
    }
    expect("12 Mb") {
      k(12 * 1024)
    }
    expect("5 Gb") {
      k(5 * 1024 * 1024)
    }
  }

}