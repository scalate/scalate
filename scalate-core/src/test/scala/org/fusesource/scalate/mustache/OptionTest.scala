/**
 * Copyright (C) 2009-2011 the original author or authors.
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
package org.fusesource.scalate.mustache

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

import org.fusesource.scalate.TemplateTestSupport

/**
 * @version $Revision: 1.1 $
 */
class OptionTest extends TemplateTestSupport {
  test("Some is unwrapped") {
    assertMoustacheOutput("Hey James!", "Hey {{name}}!", Map("name" -> Some("James")))
  }

  test("None is treated like null") {
    assertMoustacheOutput("Hey !", "Hey {{name}}!", Map("name" -> None))
  }

  test("Some is treated like a collection of 1") {
    assertMoustacheOutput("Hey James!", "{{#person}}Hey {{name}}!{{/person}}", Map("person" -> Some(Map("name" -> "James"))))
  }

  test("None is treated like a collection of 0") {
    assertMoustacheOutput("", "{{#person}}Hey {{name}}!{{/person}}", Map("person" -> None))
  }

}
