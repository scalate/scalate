# Ssp (Scala Server Pages)

* Table of contents
{:toc}

Ssp pages are like JSP/ASP pages in syntax but using Scala code for expressions instead of Java or EL like in JSP.

If you know JSP or ASP then hopefully the syntax of Ssp is familiar; only using Scala as the language of expressions and method invocations.

## Syntax

A Ssp template consists of plain text, usually an HTML document, which has special Ssp tags embedded in it so that
portions of the document are rendered dynamically.  Everything outside of a `<% ... %>` and `${ ... }` sequence 
are considered literal text and are generally passed through to the rendered document unmodified.

### Inserting Scala: `<%= %>` or `${ }`

Code wrapped by `<%=` and `%>` or with '${' and '}' is evaluated and 
the output is inserted into the document.

For example:
{pygmentize:: jsp}
<p>
  <%= List("hi", "there", "reader!").mkString(" ") %>
  ${ "yo "+(3+4) } 
</p>
{pygmentize}

is rendered as:
{pygmentize:: xml}
<p>
  hi there reader!
  yo 7
</p>
{pygmentize}

### Running Scala: `<% %>`

Code wrapped by `<%` and `%>` is evaluated but *not* inserted into the document.

For example:
{pygmentize:: jsp}
<%
  var foo = "hello"
  foo += " there"
  foo += " you!"
%>
<p>${foo}</p>
{pygmentize}

is rendered as:
{pygmentize:: xml}
<p>hello there you!</p>
{pygmentize}

### Binding Variables: `<%@ %>`

When a Scalate template is rendered, the caller can pass an attribute map
which the template in charge of rendering. To bind the attribute to a Scala
variable, a Scaml template uses the following syntax to declare the variable:
{pygmentize:: jsp}
<%@ val foo: MyType %>
{pygmentize}

If the attribute map does not contain a "foo" entry, then a 
NoValueSetException is thrown when the the template is rendered.

To avoid this exception, a default value can be configured.  For
example:
{pygmentize:: jsp}
<%@ val bar: String = "this is the default value" %>
{pygmentize}

The attribute is now available for use as an expression. 

Its very common to have a template based on a single object who's members are f
frequently accessed.  In this cases, it's convenient to import all the object's 
members.  This can be done by adding the import keyword to the attribute declaration.

For example:
{pygmentize:: jsp}
<%@ import val model: Person %>
<p>Hello ${name}, what is the weather like in ${city}</p>
{pygmentize}

is the same as:
{pygmentize:: jsp}
<%@ val model: Person %>
<% import model._ %>
<p>Hello ${name}, what is the weather like in ${city}</p>
{pygmentize}

Which is the same as:

{pygmentize:: jsp}
<%@ val model: Person %>
<p>Hello ${model.name}, what is the weather like in ${model.city}</p>
{pygmentize}

### Comments: `<%-- --%>`

Ssp comments prevent everything inside the comment markers from being inserted in to the rendered document.

{pygmentize:: jsp}
<%-- this is a comment --%>
{pygmentize}

### Includes `<% include ... %>`

You can include other scripts in your page using the include method

{pygmentize:: jsp}
<% include file="relativeOrAbsoluteURL" %>
{pygmentize}

The URL is then evaluated and included in place in your template.

### Custom tags

In JSP there is a concept of custom tags which can process a block of the 
template such as for looping or transforming content.

For example if you want to XML escape a block of a template you can just 
invoke the *xmlEscape* method.

{pygmentize:: jsp}
<%= xmlEscape {%>
  I like <strong> cheese & crackers
<% } %>
{pygmentize}

is rendered as:
{pygmentize:: xml}
I like &lt;strong&gt; cheese &amp; crackers
{pygmentize}
      
## Other Resources

* [User Guide](user-guide.html)
* [Documentation](index.html)
