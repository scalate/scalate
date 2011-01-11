# Ssp (Scala Server Pages)

* Table of contents
{:toc}

Ssp pages are like a Scala version of [Velocity](#velocity_style_directives), JSP or Erb from Rails in syntax but using Scala code for expressions instead of Java/EL/Ruby.

If you know Velocity, JSP or Erb then hopefully the syntax of Ssp is familiar; only using Scala as the language of expressions and method invocations.

## Syntax {#syntax}

A Ssp template consists of plain text, usually an HTML document, which has special Ssp tags embedded in it so that
portions of the document are rendered dynamically.  Everything outside of a `<% ... %>` and `${ ... }` sequence 
are considered literal text and are generally passed through to the rendered document unmodified.

### Expressions: `${ }` or `<%= %>`

Code wrapped by `<%=` and `%>` or with '${' and '}' is evaluated and  the output is inserted into the document.

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

### Scala code: `<% %>`

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

If you like to use [Velocity style directives](#velocity_style_directives) you can also use [`#{` `}#`](#velocity_block)

### Attributes: `<%@ %>` {#bindings}

When a Scalate template is rendered, the caller can pass an attribute map
which the template is in charge of rendering. To bind an attribute to a type safe Scala
variable an SSP template uses the following syntax to declare the attribute:

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

Its very common to have a template based on a single object who's members are
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

### Velocity style directives {#velocity_style_directives}

To perform logical branching or looping Scalate supports [Velocity](http://velocity.apache.org/) style directives.

The velocity style directives all start with a `#` and either take an expression in parens, or don't.

For example `#if` takes an expression, such as `#if (x > 5)`. There can be whitespace between the directive name and the parens if required. So you can use any of these

* `#if(x > 5)`
* `#if (x > 5)`
* `#if( x > 5 )`

When a directive doesn't take an expression you can use parens around the directive name to separate it more clearly from text.

For example if you want to generate an if/else in a single line: `#if (x > 5)a#(else)b#end`

#### `#for`

The `#for` directive is used to iterate over expressions in a Scala like way. 

{pygmentize_and_compare::}
-----------------------------
jsp: .ssp file
-----------------------------
<ul>
#for (i <- 1 to 5)
  <li>${i}</li>
#end
</ul>
-----------------------------
xml: produces
-----------------------------
<ul>
  <li>1</li>
  <li>2</li>
  <li>3</li>
  <li>4</li>
  <li>5</li>
</ul>
{pygmentize_and_compare}

Just like in the Scala language you can perform multiple nested loops using [sequence comprehensions](http://www.scala-lang.org/node/111).

For example for a nested loop of both x and y...

{pygmentize_and_compare::}
-----------------------------
jsp: .ssp file
-----------------------------
<ul>
#for (x <- 1 to 2; y <- 1 to 2)
  <li>(${x}, ${y})</li>
#end
</ul>
-----------------------------
xml: produces
-----------------------------
<ul>
  <li>(1, 1)</li>
  <li>(1, 2)</li>
  <li>(1, 1)</li>
  <li>(2, 1)</li>
</ul>
{pygmentize_and_compare}


#### `#if`

You can perform if/elseif/else branches using the `#if`, `#elseif` (or `#elif`), `#else` and `#end` directives.

The use of `#elseif` and `#else` are optional. You can just use `#if` and `#end` if you like

{pygmentize_and_compare::}
-----------------------------
jsp: .ssp file
-----------------------------
<p>
#if (customer.type == "Gold")
  Special stuff...
#end
</p>
-----------------------------
xml: produces
-----------------------------
<p>
  Special stuff...
</p>
{pygmentize_and_compare}


Or you can use each directive together, using as many `#elseif` directives as you like

{pygmentize_and_compare::}
-----------------------------
jsp: .ssp file
-----------------------------
<p>
#if (n == "James")
  Hey James
#elseif (n == "Hiram")
  Yo Hiram
#else
  Dunno
#end
</p>
-----------------------------
xml: produces
-----------------------------
<p>
  Hey James
</p>
{pygmentize_and_compare}


#### `#set` {#set}

You often want to take a section of a template and assign it to an attribute which you can then pass into a [layout]("user-guide.html#layouts") or some other template.

For example you might wish to define a head section to allow a page to define custom output to go into the HTML head element...

{pygmentize:: jsp}
#set (head)
  ... some page specific JavaScript includes here...
#end
...rest of the page here...
{pygmentize}

Then in the [layout]("user-guide.html#layouts") we could use

{pygmentize:: jsp}
<%@ var body: String %>
<%@ var title: String = "Some Default Title" %>
<%@ var head: String = "" %>
<html>
<head>
  <title>${title}</title>
  
  <%-- page specific head goes here --%>
  ${unescape(head)}
</head>
<body>
  <p>layout header goes here...</p>

  ${unescape(body)}
  
  <p>layout footer goes here...</p>
</body>
</html>
{pygmentize}



#### `#match`

You can perform Scala style pattern matching using the `#match`, `#case`, `#otherwise` and `#end` directives.

You can think of matching in Scala as being like a Java switch statement only way more powerful. 

The `#match` takes an expression to match on, then each `#case` takes a value, filter or type expression to match on.

{pygmentize_and_compare::}
-----------------------------
jsp: .ssp file
-----------------------------
<p>
#match (customer.type)
#case("Gold")
  Great stuff
#case("Silver")
  Good stuff
#otherwise
  No stuff
#end
</p>
-----------------------------
xml: produces
-----------------------------
<p>
  Special stuff...
</p>
{pygmentize_and_compare}

This example shows how you can use type expressions instead to match on

{pygmentize_and_compare::}
-----------------------------
jsp: .ssp file
-----------------------------
<p>
#match (person)
#case(m: Manager)
  ${m.name} manages ${m.manages.size} people
#case(p: Person)
  ${p.name} is not a manager
#otherwise
  Not a person
#end
</p>
-----------------------------
xml: produces
-----------------------------
<p>
  Hey James
</p>
{pygmentize_and_compare}


#### `#do` {#do}

The `#do` directive can be used to invoke a function passing a block of template as an argument such as when you want to apply a specific [layout to a block of template](user-guide.html#explicit_layouts_inside_a_template) or want to call a [function passing a template block](user-guide.html#passing_a_template_block_to_a_scala_function), a little like you might do using custom tags in JSP.

{pygmentize:: jsp}
#do(layout("someLayout.ssp"))
 this is some template output...
#end
{pygmentize}


#### `#import`

The `#import` directive can be used as an alternative to using `<% import somePackage %>` to import Scala/Java packages, classes or methods.

{pygmentize_and_compare::}
-----------------------------
jsp: .ssp file
-----------------------------
#import(java.util.Date)

<p>The time is now ${new Date}</p>
-----------------------------
xml: produces
-----------------------------
<p>The time is now Thu Apr 15 15:19:41 IST 2010</p>
{pygmentize_and_compare}


#### `#{` and `}#` scriplets {#velocity_block}

Sometimes you just want to include a couple of lines of Scala code in a template such as to define a few variables, add a few imports or whatever.

If you don't like the JSP / Erb style `<%` .. `%>` tags you can use velocity style `#{` .. `}#` instead

{pygmentize:: jsp}
#{
  import java.util.Date
  val now = new Date 
}#
Hello the time is ${now}
{pygmentize}


### Comments: `<%-- --%>`

Ssp comments prevent everything inside the comment markers from being inserted in to the rendered document.

{pygmentize:: jsp}
<%-- this is a comment --%>
{pygmentize}

### Includes `${include(someUri)}`

You can include other scripts in your page using the include method

{pygmentize:: jsp}
${include("relativeOrAbsoluteURL"}
{pygmentize}

The URL is then evaluated and included in place in your template.

## Other Resources

* [User Guide](user-guide.html)
* [Documentation](index.html)
