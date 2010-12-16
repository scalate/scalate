package org.fusesource.scalate
package wikitext

import org.junit.Assert._

class CamelSiteConfluenceTest extends AbstractConfluenceTest {

  val includeSimpleTests = false

  if (includeSimpleTests) {
  test("parse table without div") {
    val output = renderConfluence(
"""
h2. Bean Component

The *bean:* component binds beans to Camel message exchanges.

h3. Options
{div:class=confluenceTableSmall}
{div}
|| Name || Type || Default || Description ||
| {{method}} | {{String}} | {{null}} | The method name that bean will be invoked. If not provided, Camel will try to pick the method itself. In case of ambiguity an exception is thrown. See [Bean Binding] for more details. |
| {{cache}} | {{boolean}} | {{false}} | If enabled, Camel will cache the result of the first [Registry] look-up. Cache can be enabled if the bean in the [Registry] is defined as a singleton scope. |
| {{multiParameterArray}} | {{boolean}} | {{false}} | *Camel 1.5:* How to treat the parameters which are passed from the message body; if it is {{true}}, the In message body should be an array of parameters. |

You can append query options to the URI in the following format, {{?option=value&option=value&...}}

""")
    assertOccurrences(output, "<table>", 1)
  }
  }


  test("parse table with div") {
    val output = renderConfluence(
"""
h2. Bean Component

The *bean:* component binds beans to Camel message exchanges.

h3. Options
{div:class=confluenceTableSmall}
|| Name || Type || Default || Description ||
| {{method}} | {{String}} | {{null}} | The method name that bean will be invoked. If not provided, Camel will try to pick the method itself. In case of ambiguity an exception is thrown. See [Bean Binding] for more details. |
| {{cache}} | {{boolean}} | {{false}} | If enabled, Camel will cache the result of the first [Registry] look-up. Cache can be enabled if the bean in the [Registry] is defined as a singleton scope. |
| {{multiParameterArray}} | {{boolean}} | {{false}} | *Camel 1.5:* How to treat the parameters which are passed from the message body; if it is {{true}}, the In message body should be an array of parameters. |
{div}

You can append query options to the URI in the following format, {{?option=value&option=value&...}}

""")
    assertOccurrences(output, "<table>", 1)
    assertOccurrences(output, "<tr>", 4)
  }

  protected def assertOccurrences(text: String, m: String, expected: Int) {
    val actual = occurences(text, m)
    assertEquals("Occurences of '" + m, expected, actual)
  }

  protected def occurences(text: String, m: String) = {
    var count = 0;
    var idx = 0
    while (idx >= 0) {
      idx = text.indexOf(m, idx)
      if (idx >= 0) {
        idx += 1
        count += 1
      }
    }
    count
  }
}