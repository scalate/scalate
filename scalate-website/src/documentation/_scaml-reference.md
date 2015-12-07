# Scaml (Scala Markup Language)

{:toc}

Scaml is a very DRY way of writing XHTML templates.  It is Scala
version of [Haml](http://haml-lang.com/). Scaml functions as a replacement
for inline page templating systems such as PHP, ERB, and ASP. However,
Scaml avoids the need for explicitly coding XHTML into the template,
because it uses a very concise white space active XHMLT notation.

## Features

* Whitespace active
* Well-formatted markup
* DRY
* Follows CSS conventions
* Integrates Scala code
* [Haml](http://haml-lang.com/) or [Jade](http://jade-lang.com/) style 
  notation

## Haml vs Jade Notation {#jade}

Scaml supports both the original [Haml](http://haml-lang.com/) notation and a
newer [Jade](http://jade-lang.com/) notation. The Jade dialect of Haml
recognizes that Haml based notations are best used for rendering structured
XHTML markup and that content is best rendered using something like markdown.
It therefore, simplifies the element notation in exchange for complicating the
plain content notation.

{pygmentize_and_compare::}
-----------------------------
scaml: An example .scaml file
-----------------------------
%html
  %body
    The quick brown fox jumps 
    over the lazy dog
-----------------------------
jade: An equivalent .jade file
-----------------------------
html
  body
    | The quick brown fox jumps 
    | over the lazy dog
{pygmentize_and_compare}

Both examples above will render to the following:

{pygmentize:: xml}
<html>
  <body>
    The quick brown fox jumps 
    over the lazy dog
  </body>
</html>
{pygmentize}

The only difference between the Haml and Jade notation styles
are that in the Jade notation style:

* Elements do not get prefixed with `%`
* Plain text sections must be prefixed with `|`

The rest of this document will assume you are using the Haml notation
style.  All the examples will work with the Jade notation provided
you apply the differences listed above.

## Plain Text

A substantial portion of any HTML document is its content, which is plain old
text. Any Scaml line that's not interpreted as something else is taken to be
plain text, and passed through unmodified.

For example:

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%gee
  %whiz
    Wow this is cool!
-----------------------------
xml: renders to
-----------------------------
<gee>
  <whiz>
    Wow this is cool!
  </whiz>
</gee>
{pygmentize_and_compare}

Note that HTML tags are passed through unmodified as well.
If you have some HTML you don't want to convert to Scaml,
or you're converting a file line-by-line,
you can just include it as-is.

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%p
  <div id="blah">Blah!</div>
-----------------------------
xml: renders to
-----------------------------
<p>
  <div id="blah">Blah!</div>
</p>
{pygmentize_and_compare}

### Escaping: `\`

The backslash character escapes the first character of a line,
allowing use of otherwise interpreted characters as plain text.

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%title
  = title
  \= title
-----------------------------
xml: renders to
-----------------------------
<title>
  MyPage
  = title
</title>
{pygmentize_and_compare}

## HTML Elements


### Element Name: `%`

The percent character is placed at the beginning of a line.
It's followed immediately by the name of an element,
then optionally by modifiers (see below), a space,
and text to be rendered inside the element.
It creates an element in the form of `<element></element>`.

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%one
  %two
    %three Hey there
-----------------------------
xml: renders to
-----------------------------
<one>
  <two>
    <three>Hey there</three>
  </two>
</one>
{pygmentize_and_compare}

Any string is a valid element name;
Scaml will automatically generate opening and closing tags for any element.

### Attributes: `{` `}` or `(` `)` {#attributes}

Brackets represent a Scala Map
that is used for specifying the attributes of an element.
Ruby hash syntax is used instead of Scala syntax to 
preserve a higher level of compatibility with the original
Haml implementation.
It is translated and evaluated as a Scala Map,
so logic will work in it and local variables may be used.
Quote characters within the attribute
will be replaced by appropriate escape sequences.
The hash is placed after the tag is defined.

{pygmentize_and_compare::wide=true}
-----------------------------
scaml: example
-----------------------------
%html{:xmlns => "http://www.w3.org/1999/xhtml", "xml:lang" => "en", :lang => "en"}
-----------------------------
xml: renders to
-----------------------------
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"></html>
{pygmentize_and_compare}

Attribute hashes can also be stretched out over multiple lines
to accommodate many attributes.
However, newlines may only be placed immediately after commas.

{pygmentize_and_compare::wide=true}
-----------------------------
scaml: example
-----------------------------
%script{:type => "text/javascript",
        :src  => "javascripts/script"}
-----------------------------
xml: renders to
-----------------------------
<script type="text/javascript" src="javascripts/script"/>
{pygmentize_and_compare}

Complex expression are supported if you wrap them between the `{` 
and `}` characters.

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%li{:counter={3+4}} Stuff
-----------------------------
xml: renders to
-----------------------------
<li counter="7">Stuff</li>
{pygmentize_and_compare}

#### HTML-style Attributes: `()`

Scaml also supports a terser, less Scala-specific attribute syntax
based on HTML's attributes.
These are used with parentheses instead of brackets, like so:

{pygmentize:: scaml}
%html(xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en")
{pygmentize}

Scala variables can be used by omitting the quotes.
For example:

{pygmentize:: scaml}
%a(title=title href=href) Stuff
{pygmentize}

This is the same as:

{pygmentize:: scaml}
%a{:title =>title, :href => href} Stuff
{pygmentize}

Complex expression are supported if you wrap them between the `{` 
and `}` characters.

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%li(counter={3+4}) Stuff
-----------------------------
xml: renders to
-----------------------------
<li counter="7">Stuff</li>
{pygmentize_and_compare}

You can use both syntaxes together:

{pygmentize:: scaml}
%a(title="Hello"){:href => "http://scalate.github.io/scalate"} Stuff
{pygmentize}

You can also use `\#{}` interpolation to insert complicated expressions
in a HTML-style attribute:

{pygmentize:: scaml}
%span(class="widget_#{widget.number}")
{pygmentize}

HTML-style attributes can be stretched across multiple lines
just like hash-style attributes:
{pygmentize:: scaml}
%script(type="text/javascript"
        src="javascripts/script")
{pygmentize}

<!-- TODO
#### Attribute Methods

A Scala method call that returns a hash
can be substituted for the hash contents.
For example, Scaml::Helpers defines the following method:

    def html_attrs(lang = "en-US")
      {:xmlns => "http://www.w3.org/1999/xhtml", "xml:lang" => lang, :lang => lang}
    end

This can then be used in Scaml, like so:

    %html{html_attrs("fr-fr")}

This is rendered to:

    <html lang="fr-fr" xml:lang="fr-fr" xmlns="http://www.w3.org/1999/xhtml">
    </html>

You can use as many such attribute methods as you want
by separating them with commas,
like a Scala argument list.
All the hashes will me merged together, from left to right.
For example, if you defined

    def hash1
      {:bread => "white", :filling => "peanut butter and jelly"}
    end

    def hash2
      {:bread => "whole wheat"}
    end

then

    %sandwich{hash1, hash2, :delicious => true}/

would render to:

    <sandwich bread="whole wheat" delicious="true" filling="peanut butter and jelly" />

Note that the Scaml attributes list has the same syntax as a Scala method call.
This means that any attribute methods must come before the hash literal.

Attribute methods aren't supported for HTML-style attributes.
-->

#### Boolean Attributes

Some attributes, such as "checked" for `input` tags or "selected" for `option` tags,
are "boolean" in the sense that their values don't matter -
it only matters whether or not they're present.
In HTML (but not XHTML), these attributes can be written as
{pygmentize:: xml}
<input selected>
{pygmentize}

To do this in Scaml using hash-style attributes, just assign a Scala
`true` value to the attribute:
{pygmentize:: scaml}
%input{:selected => true}
{pygmentize}

In XHTML, the only valid value for these attributes is the name of the
attribute.  Thus this will render in XHTML as
{pygmentize:: xml}
<input selected="selected"/>
{pygmentize}

To set these attributes to false, simply assign them to a Scala false value.
In both XHTML and HTML
{pygmentize:: scaml}
%input{:selected => false}
{pygmentize}

will just render as

{pygmentize:: xml}
<input/>
{pygmentize}

HTML-style boolean attributes can be written just like HTML:
{pygmentize:: scaml}
%input(selected)
{pygmentize}

or using `true` and `false`:
{pygmentize:: scaml}
%input(selected=true)
{pygmentize}

### Class and ID: `.` and `#`

The period and pound sign are borrowed from CSS.
They are used as shortcuts to specify the `class`
and `id` attributes of an element, respectively.
Multiple class names can be specified in a similar way to CSS,
by chaining the class names together with periods.
They are placed immediately after the tag and before an attributes hash.

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%div#things
  %span#rice Chicken Fried
  %p.beans{ :food => "true" } The magical fruit
  %h1.class.otherclass#id La La La
-----------------------------
xml: renders to
-----------------------------
<div id="things">
  <span id="rice">Chicken Fried</span>
  <p class="beans" food="true">The magical fruit</p>
  <h1 id="id" class="class otherclass">La La La</h1>
</div>
{pygmentize_and_compare}

And,

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
#content
  .articles
    .article.title Doogie Howser Comes Out
    .article.date 2006-11-05
    .article.entry
      Neil Patrick Harris would like to dispel any rumors that he is straight
-----------------------------
xml: renders to
-----------------------------
<div id="content">
  <div class="articles">
    <div class="article title">Doogie Howser Comes Out</div>
    <div class="article date">2006-11-05</div>
    <div class="article entry">
      Neil Patrick Harris would like to dispel any rumors that he is straight
    </div>
  </div>
</div>
{pygmentize_and_compare}

### Weird Element Names: `'`element`'`

Sometimes you have to generate markup with weird element names.  Element names
like `<funny.element/>`.  Since Scaml interprets the period as a class name for the 
element, the following example:

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%funny.element
-----------------------------
xml: renders to
-----------------------------
<funny class="element"/>
{pygmentize_and_compare}

does not give you the desired result of `<funny.element/>`.  In these cases you must single
quote the element name. 

Example:

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%'funny.element'
-----------------------------
xml: renders to
-----------------------------
<funny.element/>
{pygmentize_and_compare}

#### Implicit Div Elements

Because divs are used so often, they're the default elements.
If you only define a class and/or id using `.` or `#`,
a div is automatically used.

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
#collection
  .item
    .description What a cool item!
-----------------------------
scaml: is the same as
-----------------------------
%div#collection
  %div.item
    %div.description What a cool item!
{pygmentize_and_compare}

and is rendered to:
{pygmentize:: xml}
<div id="collection">
  <div class="item">
    <div class="description">What a cool item!</div>
  </div>
</div>
{pygmentize}

### Self-Closing Tags: `/`

The forward slash character, when placed at the end of a tag definition,
causes the tag to be self-closed.
For example:

{pygmentize_and_compare::wide=true}
-----------------------------
scaml: example
-----------------------------
%br/
%meta{"http-equiv" => "Content-Type", :content => "text/html"}/
-----------------------------
xml: renders to
-----------------------------
<br/>
<meta http-equiv="Content-Type" content="text/html"/>
{pygmentize_and_compare}

Some tags are automatically closed, as long as they have no content.
`meta`, `img`, `link`, `script`, `br`, and `hr` tags are closed by default.
This list can be customized by setting the [`ScamlOptions.autoclose`](http://scalate.github.io/scalate/maven/${project_version}/scalate-core/scaladocs/org/fusesource/scalate/scaml/ScamlOptions$.html) option.

{pygmentize_and_compare::wide=true}
-----------------------------
scaml: example
-----------------------------
%br
%meta{"http-equiv" => "Content-Type", :content => "text/html"}
-----------------------------
xml: renders to
-----------------------------
<br/>
<meta http-equiv="Content-Type" content="text/html"/>
{pygmentize_and_compare}

### Whitespace Removal: `>` and `<`

`>` and `<` give you more control over the whitespace near a tag.
`>` will remove all whitespace surrounding a tag,
while `<` will remove all whitespace immediately within a tag.
You can think of them as alligators eating the whitespace:
`>` faces out of the tag and eats the whitespace on the outside,
and `<` faces into the tag and eats the whitespace on the inside.
They're placed at the end of a tag definition,
after class, id, and attribute declarations
but before `/` or `=`.

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%blockquote<
  %div
    Foo!
-----------------------------
xml: renders to
-----------------------------
<blockquote><div>
  Foo!
</div></blockquote>
{pygmentize_and_compare}

And:
{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%img
%img>
%img
-----------------------------
xml: renders to
-----------------------------
<img/><img/><img/>
{pygmentize_and_compare}

And:
{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%p<= "Foo\nBar"
-----------------------------
xml: renders to
-----------------------------
<p>Foo
Bar</p>
{pygmentize_and_compare}

And finally:
{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%img
%pre><
  foo
  bar
%img
-----------------------------
xml: renders to
-----------------------------
<img /><pre>foo
bar</pre><img />
{pygmentize_and_compare}

## Doctype: `!!! format`

When describing HTML documents with Scaml,
you can have a document type or XML prolog generated automatically
by including the characters `!!!`.

{pygmentize_and_compare::wide=true}
-----------------------------
scaml: example
-----------------------------
!!! XML
!!!
%html
  %head
    %title Myspace
  %body
    %h1 I am the international space station
    %p Sign my guestbook
-----------------------------
xml: renders to
-----------------------------
<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
    <title>Myspace</title>
  </head>
  <body>
    <h1>I am the international space station</h1>
    <p>Sign my guestbook</p>
  </body>
</html>
{pygmentize_and_compare}

You can also specify the specific doctype after the `!!!`
When the `format` is set to `:xhtml` (the default),
the following doctypes are supported:

#### `!!!`
> XHTML 1.0 Transitional
> `<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">`

#### `!!! Strict`
> XHTML 1.0 Strict
> `<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">`

#### `!!! Frameset`
> XHTML 1.0 Frameset<br/>
> `<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">`

#### `!!! 1.1`
> XHTML 1.1<br/>
> `<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">`

#### `!!! Basic`
> XHTML Basic 1.1<br/>
> `<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML Basic 1.1//EN" "http://www.w3.org/TR/xhtml-basic/xhtml-basic11.dtd"> `

#### `!!! Mobile`
> XHTML Mobile 1.2<br/>
> `<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.2//EN" "http://www.openmobilealliance.org/tech/DTD/xhtml-mobile12.dtd">`

When the `format` option is set to `:html4`,
the following doctypes are supported:

#### `!!!`
> HTML 4.01 Transitional<br/>
> `<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">`

#### `!!! Strict`
> HTML 4.01 Strict<br/>
> `<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">`

#### `!!! Frameset`
> HTML 4.01 Frameset<br/>
> `<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">`

#### `!!! 5`
> HTML 5<br/>
> `<!DOCTYPE html>`<br/>

When the `format` option is set to `:html5`,
`!!!` is always `<!DOCTYPE html>`.

If you're not using the UTF-8 character set for your document,
you can specify which encoding should appear
in the XML prolog in a similar way.


{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
!!! XML iso-8859-1
-----------------------------
xml: renders to
-----------------------------
<?xml version="1.0" encoding="iso-8859-1" ?>
{pygmentize_and_compare}

## Comments

Scaml supports two sorts of comments:
those that show up in the HTML output
and those that don't.

### HTML Comments: `/`

The forward slash character, when placed at the beginning of a line,
wraps all text after it in an HTML comment.

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%peanutbutterjelly
  / This is the comment
  I like sandwiches!
-----------------------------
xml: renders to
-----------------------------
<peanutbutterjelly>
  <!-- This is the comment -->
  I like sandwiches!
</peanutbutterjelly>
{pygmentize_and_compare}

The forward slash can also wrap indented sections of code. For example:

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
/
  %p This doesn't render...
  %div
    %h1 Because it's commented out!
-----------------------------
xml: renders to
-----------------------------
<!--
  <p>This doesn't render...</p>
  <div>
    <h1>Because it's commented out!</h1>
  </div>
-->
{pygmentize_and_compare}

#### Conditional Comments: `/[]`

You can also use [Internet Explorer conditional comments](http://www.quirksmode.org/css/condcom.html)
by enclosing the condition in square brackets after the `/`.

{pygmentize_and_compare::wide=true}
-----------------------------
scaml: example
-----------------------------
/[if IE]
  %a{ :href => "http://www.mozilla.com/en-US/firefox/" }
    %h1 Get Firefox
-----------------------------
xml: renders to
-----------------------------
<!--[if IE]>
  <a href="http://www.mozilla.com/en-US/firefox/">
    <h1>Get Firefox</h1>
  </a>
<![endif]-->
{pygmentize_and_compare}

### Scaml Comments: `-#`

The hyphen followed immediately by the pound sign
signifies a silent comment.
Any text following this isn't rendered in the resulting document
at all.

For example:
{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%p foo
-# This is a comment
%p bar
-----------------------------
xml: renders to
-----------------------------
<p>foo</p>
<p>bar</p>
{pygmentize_and_compare}

You can also nest text beneath a silent comment.
None of this text will be rendered.


{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%p foo
-#
  This won't be displayed
    Nor will this
%p bar
-----------------------------
xml: renders to
-----------------------------
<p>foo</p>
<p>bar</p>
{pygmentize_and_compare}

## Scala Evaluation

### Binding Attributes `-@` {#bindings}

When a Scalate template is rendered, the caller can pass an attribute map
which the template in charge of rendering. To bind the attribute to a Scala
variable, a Scaml template uses the hyphen character followed by an at sign 
and then a Scala variable declaration statement.

For example To define an attribute use the following declaration
{pygmentize:: scaml}
-@ val foo: MyType 
{pygmentize}

If the attribute map does not contain a "foo" entry, then a 
NoValueSetException is thrown when the the template is rendered.

To avoid this exception, a default value can be configured.  For
example:
{pygmentize:: scaml}
-@ val bar: String = "this is the default value"
{pygmentize}

The attribute is now available for use as an expression. 

Its very common to have a template based on a single object who's members are f
frequently accessed.  In this cases, it's convenient to import all the object's 
members.  This can be done by adding the import keyword to the attribute declaration.

For example:
{pygmentize:: scaml}
-@ import val model: Person
%p Hello #{name}, what is the weather like in #{city}
{pygmentize}

is the same as:
{pygmentize:: scaml}
-@ val model: Person
- import model._
%p Hello #{name}, what is the weather like in #{city}
{pygmentize}

Which is the same as:
{pygmentize:: scaml}
-@ val model: Person
%p Hello #{model.name}, what is the weather like in #{model.city}
{pygmentize}


### Inserting Scala: `=`

The equals character is followed by Scala code.
This code is evaluated and the output is inserted into the document.

{pygmentize_and_compare::wide=true}
-----------------------------
scaml: example
-----------------------------
%p
  = List("hi", "there", "reader!").mkString(" ")
  = "yo"
-----------------------------
xml: renders to
-----------------------------
<p>
  hi there reader!
  yo
</p>
{pygmentize_and_compare}

The default setting for the [`TemplateEngine.escapeMarkup`](http://scalate.github.io/scalate/maven/${project_version}/scalate-core/scaladocs/org/fusesource/scalate/TemplateEngine.html) option is 
true.  When `TemplateEngine.escapeMarkup` is enabled, `=` will sanitize any
HTML-sensitive characters generated by the script. 

{pygmentize_and_compare::wide=true}
-----------------------------
scaml: example
-----------------------------
= """<script>alert("I'm evil!");</script>"""
-----------------------------
xml: renders to
-----------------------------
&lt;script&gt;alert(&quot;I'm evil!&quot;);&lt;/script&gt;
{pygmentize_and_compare}

`=` can also be used at the end of a tag to insert Scala code within that tag.

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%p= "hello"
-----------------------------
xml: renders to
-----------------------------
<p>hello</p>
{pygmentize_and_compare}

### Running Scala: `-`

The hyphen character is also followed by Scala code.
This code is evaluated but *not* inserted into the document.

**It is not recommended that you use this widely;
almost all processing code and logic should be restricted
to the Controller, the Helper, or partials.**

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
- var foo = "hello"
- foo += " there"
- foo += " you!"
%p= foo
-----------------------------
xml: renders to
-----------------------------
<p>hello there you!</p>
{pygmentize_and_compare}

Or alternatively, if you have a large block of Scala code, you can
nest it under the hyphen character as demonstrated by the following example:

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
-
  var foo = "hello"
      foo += " there"
  foo += " you!"
%p= foo
-----------------------------
xml: renders to
-----------------------------
<p>hello there you!</p>
{pygmentize_and_compare}

#### Scala Blocks

Scala blocks, like XHTML tags, don't need to be explicitly closed in Scaml.
Rather, they're automatically closed, based on indentation.
A block begins whenever the indentation is increased
after a Scala insertion or evaluation command.
It ends when the indentation decreases.

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
- for(i <- 42 to 46)
  %p= i
%p See, I can count!
-----------------------------
xml: renders to
-----------------------------
<p>42</p>
<p>43</p>
<p>44</p>
<p>45</p>
<p>46</p>
<p>See, I can count!</p>
{pygmentize_and_compare}

And,

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%p
  - 2 match
    - case 1 =>
      = "one"
    - case 2 =>
      = "two"
    - case 3 =>
      = "three"
-----------------------------
xml: renders to
-----------------------------
<p>
  two
</p>
{pygmentize_and_compare}
    
When inserting evaluated statements, it can also take advantage of Scala blocks. It can be handy
for passing partial functions.  

For example:
{pygmentize:: scaml}
%p
  = List(1,2,3).foldLeft("result: ")
    - (a,x)=>
      - a+x 
{pygmentize}

is the same as:
{pygmentize:: scaml}
%p
  = List(1,2,3).foldLeft("result: ") { (a,x)=> { a+x } }
{pygmentize}

would be rendered to:
{pygmentize:: xml}
<p>
  result: 123
</p>
{pygmentize}
    
### Whitespace Preservation: `~` {#tilde}

`~` works just like `=`, except that it preserves the white space 
formating on its input.

Scaml always produces HTML source which is easy to read since
it properly indented.  Even dynamically generated output is 
properly indented. 

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%html
  %p
    = "line1\nline2\nline3"
-----------------------------
xml: renders to
-----------------------------
<html>
  <p>
    line1
    line2
    line3
  </p>
</html>
{pygmentize_and_compare}

Sometimes you don't want Scaml to indent the dynamically generated content.
For example, tags like `pre` and `textarea` are whitespace-sensitive;
indenting the text makes them render wrong.

When you use `~` instead of `=`,
Scaml will convert newlines to the XHTML newline escape code, `&#x000A;` and avoid
adding spaces for indentation.  

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%html
  %pre
    ~ "line1\nline2\nline3"
-----------------------------
xml: renders to
-----------------------------
<html>
  <pre>
    line1&#x000A;line2&#x000A;line3
  </pre>
</html>
{pygmentize_and_compare}


#### Ugly Preservation: `~~` {#tilde-tilde}

Sometimes, you don't want Scaml to indent or apply the whitespace transformation on
the evaluated expression. When this is the case, use `~~` to use ugly whitespace
preservation.  We call it ugly because the produce HTML will not properly indented.

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%html
  %p
    ~~ "line1\nline2\nline3"
-----------------------------
xml: renders to
-----------------------------
<html>
  <p>
line1
line2
line3
  </p>
</html>
{pygmentize_and_compare}

### Scala Interpolation: `#{}`

Scala code can be interpolated within plain text using `#{}`.

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%p This is #{quality} cake!
-----------------------------
xml: is the same as
-----------------------------
%p= "This is "+(quality)+" cake!"
{pygmentize_and_compare}

and renders to
{pygmentize:: xml}
<p>This is scrumptious cake!</p>
{pygmentize}
    
Backslashes can be used to escape `#{` strings,
but they don't act as escapes anywhere else in the string.

{pygmentize_and_compare::wide=true}
-----------------------------
scaml: example
-----------------------------
%p
  A slash make a difference here: \#{name} is set to: \\#{name}
  But is ignored for: \# or \\
-----------------------------
xml: renders to
-----------------------------
<p>
  A slash make a difference here: #{name} is set to: \Hiram
  But is ignored for: \# or \\
</p>
{pygmentize_and_compare}

<!--
Interpolation can also be used within [filters](#filters).
For example:

    :javascript
      $(document).ready(function() {
        alert(#{message.to_json});
      });

might compile to

    <script type="text/javascript">
      //<![CDATA[
        $(document).ready(function() {
          alert("Hi there!");
        });
      //]]>
    </script>
-->

### Escaping HTML: `&=` {#escaping_html}

An ampersand followed by one or two equals characters
evaluates Scala code just like the equals without the ampersand,
but sanitizes any HTML-sensitive characters in the result of the code.

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
&= "I like cheese & crackers"
-----------------------------
xml: renders to
-----------------------------
I like cheese &amp; crackers
{pygmentize_and_compare}


When the [`TemplateEngine.escapeMarkup`](http://scalate.github.io/scalate/maven/${project_version}/scalate-core/scaladocs/org/fusesource/scalate/TemplateEngine.html) option is set
to true, `=` behaves identically to `&=`.

`&` can also be used on its own so that `#{}` interpolation is escaped.

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
& I like #{"cheese & crackers"}
-----------------------------
xml: renders to
-----------------------------
I like cheese &amp; crackers
{pygmentize_and_compare}

### Unescaping HTML: `!=` {#unescaping_html}

An exclamation mark followed by one or two equals characters
evaluates Scala code just like the equals would,
but never sanitizes the HTML.

When the [`TemplateEngine.escapeMarkup`](http://scalate.github.io/scalate/maven/${project_version}/scalate-core/scaladocs/org/fusesource/scalate/TemplateEngine.html) option is set to false, `=` behaves identically to `!=`.

However, if the [`TemplateEngine.escapeMarkup`](http://scalate.github.io/scalate/maven/${project_version}/scalate-core/scaladocs/org/fusesource/scalate/TemplateEngine.html) option is set to true, `=` will sanitize the HTML, but `!=` still won't.

For example, if `TemplateEngine.escapeMarkup` is true:

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
= "I feel <strong>!"
!= "I feel <strong>!"
-----------------------------
xml: renders to
-----------------------------
I feel &lt;strong&gt;!
I feel <strong>!
{pygmentize_and_compare}

`!` can also be used on its own so that `#{}` interpolation is unescaped.

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
! I feel #{"<strong>"}!
-----------------------------
xml: renders to
-----------------------------
I feel <strong>!
{pygmentize_and_compare}

## Filters: `:` {#filters}

The colon character designates a filter.
This allows you to pass an indented block of text as input
to another filtering program and add the result to the output of Haml.

The syntax is a colon followed by an optional list of filter flags and then a colon
separated list of filter names.

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%p
  :markdown
    Markdown
    ========
    
    Hello, *World*
-----------------------------
xml: renders to
-----------------------------
<p>
  <h1>Markdown</h1>

  <p>Hello, <em>World</em></p>
</p>
{pygmentize_and_compare}

### Filter Interpolation

If you use the `!` or `&` filter flags, you can have Scala code 
interpolated with `#{}` expressions.  It is invalid to use both
the `!` and `&` flags at the same time. 

The `&` flag enables sanitized interpolations.  

{pygmentize_and_compare::wide=true}
-----------------------------
scaml: example
-----------------------------
- var flavor = "<raspberry/>"
#content
  :&markdown
    I *really* prefer #{flavor} jam.
-----------------------------
xml: renders to
-----------------------------
<div id="content">
  <p>I <em>really</em> prefer &lt;raspberry/&gt; jam.</p>
</div>
{pygmentize_and_compare}

The `!` flag enables non-sanitized interpolations.

{pygmentize_and_compare::wide=true}
-----------------------------
scaml: example
-----------------------------
- var flavor = "<raspberry/>"
#content
  :!markdown
    I *really* prefer #{flavor} jam.
-----------------------------
xml: renders to
-----------------------------
<div id="content">
  <p>I <em>really</em> prefer <raspberry/>; jam.</p>
</div>
{pygmentize_and_compare}

### Filter Whitespace Preservation

The `~` filter flag enables preserves the white space of the content.
The indent level is left unchanged and newlines are converted to `&#x000A;`

{pygmentize_and_compare::wide=true}
-----------------------------
scaml: example
-----------------------------
%html
  %p<
    :~plain
          Indentation levels are not enforced in filters.
        #{Interpolation} is disabled by default
      Last line
-----------------------------
xml: renders to
-----------------------------
<html>
  <p>    Indentation levels are not enforced in filters.&#x000A;  #{Interpolation} is disabled by default&#x000A;Last line</p>
</html>
{pygmentize_and_compare}

### Filter Chaining

More than one filter can be be used by separating each filter name with a colon.  When
this is done, the filters are chained together so that the output of filter on right, is
passed as input to the filter on the left.

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%pre
  :escaped :javascript
    alert("Hello");
-----------------------------
xml: renders to
-----------------------------
<pre>
  &lt;script type='text/javascript'&gt;
    //&lt;![CDATA[
      alert(&quot;Hello&quot;);
    //]]&gt;
  &lt;/script&gt;
</pre>
{pygmentize_and_compare}

### Available Filters

Scaml has the following filters defined:

#### `:plain` {#plain-filter}
> Does not parse the filtered text.
> This is useful for large blocks of text or HTML.  Really handy when
> when you don't want lines starting with `.` or `-` to be parsed.

#### `:javascript` {#javascript-filter}
> Surrounds the filtered text with `<script>` and CDATA tags.
> Useful for including inline Javascript.

#### `:css` {#css-filter}
> Surrounds the filtered text with `<style>` and CDATA tags.
Useful for including inline CSS.

{#cdata-filter}
#### `:cdata`
> Surrounds the filtered text with CDATA tags.

#### `:escaped` {#escaped-filter}
> Works the same as plain, but HTML-escapes the text
> before placing it in the document.

<!--
#### `:ruby` {#ruby-filter}
Parses the filtered text with the normal Ruby interpreter.
All output sent to `$stdout`, like with `puts`,
is output into the Haml document.
Not available if the [`:suppress_eval`](#suppress_eval-option) option is set to true.
The Ruby code is evaluated in the same context as the Haml template.

#### `:erb` {#erb-filter}
Parses the filtered text with ERb, like an RHTML template.
Not available if the [`:suppress_eval`](#suppress_eval-option) option is set to true.
Embedded Ruby code is evaluated in the same context as the Haml template.
-->

#### `:sass` {#sass-filter}
> Parses the filtered text with [Sass](http://sass-lang.com/) to produce CSS output.  
Only works if you have the `scalate-jruby` module on the class path.  You normally 
want to combine with the `:css` filter.  For example `:css:sass`

#### `:scss` {#scss-filter}
> Parses the filtered text with [Scss](http://sass-lang.com/) to produce CSS output.
Only works if you have the `scalate-jruby` module on the class path.  You normally 
want to combine with the `:css` filter.  For example `:css:scss`

#### `:textile` {#textile-filter}
> Parses the filtered text with [Textile](http://www.textism.com/tools/textile).
Only works the scalate-wikitext module is found on the class path.

#### `:markdown` {#markdown-filter}
> Parses the filtered text with [Markdown](http://daringfireball.net/projects/markdown).
Only works if [scalamd](http://scalamd.fusesource.org/) or [MarkdownJ](http://markdownj.org/) is found on the class path.

<!--
### Custom Filters

You can also define your own filters. See Haml::Filters for details.
-->

## Global Scaml Options

There  are several global options you can configure to customize how Scaml renders the
output.  You will need to configure these before any of your scaml templates are compiled
as they affect the generated scala template classes.

### `ScamlOptions.indent`

The `ScamlOptions.indent` option is used to control what kind of indenting characters to 
use in the rendered markup.  It defaults to two spaces but can be set to the tab character
or set to the empty string to disable indenting alltogether.

{pygmentize_and_compare::}
-----------------------------
scaml: ScamlOptions.indent = ""
-----------------------------
%gee
  %whiz
    Wow this is cool!
-----------------------------
xml: renders to
-----------------------------
<gee>
<whiz>
Wow this is cool!
</whiz>
</gee>
{pygmentize_and_compare}

### `ScamlOptions.nl`

The `ScamlOptions.nl` option is used to control what kind of new line seperator to 
use in the rendered markup.  It defaults to `\n`.  Some folks may want to set it and the indent
to the empty string to reduce the generated document sizes.

For example, if `ScamlOptions.indent = ""` and `ScamlOptions.nl = ""` then:

{pygmentize_and_compare::}
-----------------------------
scaml: example
-----------------------------
%gee
  %whiz
    Wow this is cool!
-----------------------------
xml: renders to
-----------------------------
<gee><whiz>Wow this is cool!</whiz></gee>
{pygmentize_and_compare}

### `ScamlOptions.ugly`

Enabling the `ScamlOptions.ugly` option makes `=` statements work like `~~` statements.  The dynamic expressions
will not be indented and they the new line preservation transformation method will not be applied.  Enabling the
ugly option will significantly reduce the CPU overhead of processing dynamic expressions.

{pygmentize_and_compare::}
-----------------------------
scaml: ScamlOptions.ugly = true
-----------------------------
%html
  %p
    = "line1\nline2\nline3"
-----------------------------
xml: renders to
-----------------------------
<html>
  <p>
line1
line2
line3
  </p>
</html>
{pygmentize_and_compare}

## Other Resources

* [User Guide](user-guide.html)
* [Documentation](index.html)


