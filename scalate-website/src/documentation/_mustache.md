# Mustache

* Table of contents
{:toc}

## Introduction

Scalate's Mustache is a Scala/Java implementation of the [Mustache](http://mustache.github.com/) template language.

Mustache provides logic-less templates which also work inside the browser using [mustache.js](http://github.com/janl/mustache.js) - so ideal for HTML templates which your designers own. 

You can use a regular HTML file as the template and let your designer own it, using a little JavaScript file inject the template with sample data. Your Scala/Java developer can then use the template and inject the values on the server side using the real services and domain model.

A typical Mustache template:

{pygmentize:: text}
Hello {{name}} 
You have just won ${{value}}!
{{#in_ca}}
Well, ${{taxed_value}}, after taxes.
{{/in_ca}}
{pygmentize}

Given the following attributes:

{pygmentize:: scala}
Map(
  "name" -> "Chris",
  "value" -> 10000,
  "taxed_value" -> 10000 - (10000 * 0.4),
  "in_ca" -> true
  )
{pygmentize}

Will produce the following:

{pygmentize:: text}
Hello Chris
You have just won $10000!
Well, $6000.0, after taxes.
{pygmentize}

## Syntax

Mustache works using tags which are surrounded by mustaches **{{someTag}}**. There are various kinds of tag

### Variables

The most basic tag type is the variable. A **{{name}}** tag just tries to lookup *name* in the current context and if there is no name then nothing is rendered.

All values are HTML escaped by default. If you want to return unescaped HTML use the triple mustache **{{{name}}}**. Or you can use **{{& name}}**

You can customize how a null value or empty string is rendered by configuring properties on [RenderContext](http://scalate.github.io/scalate/maven/${project_version}/scalate-core/scaladocs/org/fusesource/scalate/RenderContext.html) such as **nullString** or **noneString**

### Sections

Sections render blocks of text one or more times, depending on the value of the key in the current context.

A section begins with **{{\#foo}}** and ends with **{{/foo}}**. The behavior of the section is defined by the value of the key.

#### Empty Lists, false or None

If a key exists and is false, None or an empty collection then the section will not render anything. 

Template:

{pygmentize:: text}
Shown.
{{#nothin}}
  Never shown!
{{/nothin}}
{pygmentize}

Given the following attributes:

{pygmentize:: scala}
Map("person" -> true)
{pygmentize}

Will produce the following:

{pygmentize:: text}
Shown.
{pygmentize}

#### Non Empty Lists

If the value is a non-empty list the section will be displayed multiple times. In each case the context of the section will be set to the item in the list.

Template:

{pygmentize:: text}
{{#repo}}
  <b>{{name}}</b>
{{/repo}}
{pygmentize}

Given the following attributes:

{pygmentize:: scala}
Map(
  "repo" -> List(
    Map("name" -> "resque"),
    Map("name" -> "hub"),
    Map("name" -> "rip")
  )
)
{pygmentize}

Will produce the following:

{pygmentize:: text}
<b>resque</b>
<b>hub</b>
<b>rip</b>
{pygmentize}

#### Functions

When the value is a function which takes a String parameter then it will be invoked with the text of the section.

{pygmentize:: text}
{{#wrapped}}
  {{name}} is awesome.
{{/wrapped}}
{pygmentize}

Given the following attributes:

{pygmentize:: scala}
Map(
  "name" -> "Willy"
  "wrapped" -> ((text: String) => <b>{text}</b>)
)
{pygmentize}

Will produce the following:

{pygmentize:: text}
<b>Willy is awesome.</b>
{pygmentize}


#### Non False Values

When the value is not false, None or a collection it will be used as the context for a single rendering of the block

{pygmentize:: text}
{{#person?}}
  Hi {{name}}!
{{/person?}}
{pygmentize}

Given the following attributes:

{pygmentize:: scala}
Map(
  "person?" -> Map("name" -> "Jon")
)
{pygmentize}

Will produce the following:

{pygmentize:: text}
Hi Jon!
{pygmentize}


### Inverted Sections

An inverted section begins with a caret (hat) and ends with a slash. That is **{{^person}}** begins a "person" inverted section while **{{/person}}** ends it.

While sections can be used to render text one or more times based on the value of the key, inverted sections may render text once based on the inverse value of the key. That is, they will be rendered if the key doesn't exist, is false, or is an empty list.

{pygmentize:: text}
{{#repo}}
  <b>{{name}}</b>
{{/repo}}
{{^repo}}
  No repos :(
{{/repo}}
{pygmentize}

Given the following attributes:

{pygmentize:: scala}
Map(
  "repo" -> 
)
{pygmentize}

Will produce the following:

{pygmentize:: text}
No repos :(
{pygmentize}

### Comments

Comments begin with a bang and are ignored. The following template:

{pygmentize:: text}
<h1>Today{{! ignore me }}.</h1>
{pygmentize}

Will produce the following:

{pygmentize:: text}
<h1>Today.</h1>
{pygmentize}

Comments may contain newlines.

### Partials

Partials begin with a greater than sign, like **{{> box}}**.

Partials are rendered at runtime (as opposed to compile time), so recursive partials are possible. Just avoid infinite loops.

They also inherit the calling context so you don't have to pass in state.

In this way you may want to think of partials as includes, or template expansion, even though it's not literally true.

For example, this template and partial:

base.mustache:

{pygmentize:: text}
<h2>Names</h2>
{{#names}}
  {{> user}}
{{/names}}
{pygmentize}

user.mustache:

{pygmentize:: text}
<strong>{{name}}</strong>
{pygmentize}

Can be thought of as a single, expanded template:

{pygmentize:: text}
<h2>Names</h2>
{{#names}}
  <strong>{{name}}</strong>
{{/names}}
{pygmentize}

### Set Delimiter

Set Delimiter tags start with an equal sign and change the tag delimiters from {{ and }} to custom strings.

Consider the following contrived example:

{pygmentize:: text}
* {{default_tags}}
{{=<% %>=}}
* <% erb_style_tags %>
<%={{ }}=%>
* {{ default_tags_again }}
{pygmentize}

Here we have a list with three items. The first item uses the default tag style, the second uses erb style as defined by the Set Delimiter tag, and the third returns to the default style after yet another Set Delimiter declaration.

According to ctemplates, this "is useful for languages like TeX, where double-braces may occur in the text and are awkward to use for markup."

Custom delimiters may not contain whitespace or the equals sign.

## Layouts

The way the layouts work with the other template languages in Scalate is you define attributes inside the templates which are then passed into the layout template as template attributes. However Mustache has no 'set attribute' syntax since its a 'no logic in the template' style.

The Mustache approach is inside a layout template we can use the **{{#html}}** section to navigate the HTML of the template output.

For example this template [sample.mustache](https://github.com/scalate/scalate/blob/master/scalate-core/src/test/resources/org/fusesource/scalate/mustache/sample.mustache) we could apply this layout [mylayout.mustache](https://github.com/scalate/scalate/blob/master/scalate-core/src/test/resources/org/fusesource/scalate/mustache/mylayout.mustache) to generate [this output](https://github.com/scalate/scalate/blob/master/scalate-core/src/test/scala/org/fusesource/scalate/mustache/LayoutTest.scala#L30)

Inside the **{{#html}}** section we can then pull out child elements by name.  So inside the **{{#head}}** section you can refer to **{{title}}** and you'll get the entire &lt;title&gt; element (attributes and all). 

If you want just the children an element, create a section for it and use **{{_}}** (e.g. the way we exclude the &lt;body&gt; element in the layout but just use all the children). Names starting with @ refer to attributes. (This is using the **\** method on [NodeSeq](http://www.scala-lang.org/api/rc/scala/xml/NodeSeq.html) underneath).



## Other Resources

* [Mustache reference](http://mustache.github.com/mustache.5.html)
* [Other Mustache implementations](http://mustache.github.com/)