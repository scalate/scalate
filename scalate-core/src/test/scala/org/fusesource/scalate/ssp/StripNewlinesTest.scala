/**
 * Copyright (C) 2012 the original author or authors.
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
package org.fusesource.scalate
package ssp

class StripNewlinesTest extends TemplateTestSupport {
  showOutput = true

  test("basic newline removal") {
    assertSspOutput(
      "this should be merged into one line",
      "this should be merged \\\ninto one line")
    assertSspOutput(
      "this should be merged into one line",
      "this should be merged \\\r\ninto one line")
  }

  test("removal should work at the start of a line") {
    assertSspOutput(
      "this is one line",
      "\\\nthis is one line")
    assertSspOutput(
      "this is one line",
      "\\\r\nthis is one line")
  }

  test("removal should work at the end of a line") {
    assertSspOutput(
      "this is one line",
      "this is one line\\\n")
    assertSspOutput(
      "this is one line",
      "this is one line\\\r\n")
  }

  test("backslash should only remove one newline") {
    assertSspOutput(
      "this should be merged \ninto two lines not three",
      "this should be merged \\\n\ninto two lines not three")
    assertSspOutput(
      "this should be merged \r\ninto two lines not three",
      "this should be merged \\\r\n\r\ninto two lines not three")
  }

  test("double backslash is not an escape character") {
    assertSspOutput(
      "this should stay as \\\ntwo lines",
      "this should stay as \\\\\ntwo lines")
    assertSspOutput(
      "this should stay as \\\r\ntwo lines",
      "this should stay as \\\\\r\ntwo lines")
  }

  test("multiple escapes in one string") {
    assertSspOutput(
      "line one\nline two and more line two\nline three and backslash\\\nline four ",
      "line one\nline two\\\n and more line two\nline three and backslash\\\\\nline four \\\n")
    assertSspOutput(
      "line one\r\nline two and more line two\r\nline three and backslash\\\r\nline four ",
      "line one\r\nline two\\\r\n and more line two\r\nline three and backslash\\\\\r\nline four \\\r\n")
  }

  test("mixed newline types work as expected") {
    assertSspOutput(
      "line one\nline two and more line two\r\nline three and backslash\\\nline four ",
      "line one\nline two\\\n and more line two\r\nline three and backslash\\\\\nline four \\\r\n")
  }
}
