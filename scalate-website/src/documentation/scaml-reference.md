# Scaml (Scala Markup Language)

* Table of contents
{:toc}

Scaml is a markup language
that's used to cleanly and simply describe the XHTML of any web document,
without the use of inline code.  It is Scala version of
[Haml](http://haml-lang.com/).
Scaml functions as a replacement
for inline page templating systems such as PHP, ERB, and ASP.
However, Scaml avoids the need for explicitly coding XHTML into the template,
because it is actually an abstract description of the XHTML,
with some code to generate dynamic content.

## Features

* Whitespace active
* Well-formatted markup
* DRY
* Follows CSS conventions
* Integrates Scala code
* Implements templates with the .scaml extension


## Plain Text

A substantial portion of any HTML document is its content,
which is plain old text.
Any Scaml line that's not interpreted as something else
is taken to be plain text, and passed through unmodified.
For example:
{pygmentize:: text}
%gee
  %whiz
    Wow this is cool!
{pygmentize}

is rendered to:
{pygmentize:: xml}
<gee>
  <whiz>
    Wow this is cool!
  </whiz>
</gee>
{pygmentize}

Note that HTML tags are passed through unmodified as well.
If you have some HTML you don't want to convert to Scaml,
or you're converting a file line-by-line,
you can just include it as-is.
For example:
{pygmentize:: text}
%p
  <div id="blah">Blah!</div>
{pygmentize}

is rendered to:
{pygmentize:: xml}
<p>
  <div id="blah">Blah!</div>
</p>
{pygmentize}

### Escaping: `\`

The backslash character escapes the first character of a line,
allowing use of otherwise interpreted characters as plain text.
For example:
{pygmentize:: text}
%title
  = title
  \= title
{pygmentize}

is rendered to:
{pygmentize:: xml}
<title>
  MyPage
  = title
</title>
{pygmentize}

## HTML Elements


### Element Name: `%`

The percent character is placed at the beginning of a line.
It's followed immediately by the name of an element,
then optionally by modifiers (see below), a space,
and text to be rendered inside the element.
It creates an element in the form of `<element></element>`.
For example:
{pygmentize:: text}
%one
  %two
    %three Hey there
{pygmentize}

is rendered to:
{pygmentize:: xml}
<one>
  <two>
    <three>Hey there</three>
  </two>
</one>
{pygmentize}

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
For example:
{pygmentize:: text}
%html{:xmlns => "http://www.w3.org/1999/xhtml", "xml:lang" => "en", :lang => "en"}
{pygmentize}

is rendered to:
{pygmentize:: xml}
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"></html>
{pygmentize}

Attribute hashes can also be stretched out over multiple lines
to accommodate many attributes.
However, newlines may only be placed immediately after commas.
For example:
{pygmentize:: text}
%script{:type => "text/javascript",
        :src  => "javascripts/script"}
{pygmentize}

is rendered to:
{pygmentize:: xml}
<script type="text/javascript" src="javascripts/script"/>
{pygmentize}

Complex expression are supported if you wrap them between the `{` 
and `}` characters.
For example:
{pygmentize:: text}
%li{:counter={3+4}} Stuff
{pygmentize}

Would render as:
{pygmentize:: xml}
<li counter="7">Stuff</li>
{pygmentize}

#### HTML-style Attributes: `()`

Scaml also supports a terser, less Scala-specific attribute syntax
based on HTML's attributes.
These are used with parentheses instead of brackets, like so:
{pygmentize:: text}
%html(xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en")
{pygmentize}

Scala variables can be used by omitting the quotes.
For example:

    %a(title=title href=href) Stuff

This is the same as:

    %a{:title =>title, :href => href} Stuff

Complex expression are supported if you wrap them between the `{` 
and `}` characters.
For example:
{pygmentize:: text}
%li(counter={3+4}) Stuff
{pygmentize}

Would render as:
{pygmentize:: xml}
<li counter="7">Stuff</li>
{pygmentize}

You can use both syntaxes together:
{pygmentize:: xml}
%a(title="Hello"){:href => "http://scalate.fusesource.org"} Stuff
{pygmentize}

You can also use `#{}` interpolation to insert complicated expressions
in a HTML-style attribute:

{pygmentize:: xml}
%span(class="widget_#{widget.number}")
{pygmentize}

HTML-style attributes can be stretched across multiple lines
just like hash-style attributes:
{pygmentize:: text}
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

<!-- TODO
#### Boolean Attributes

Some attributes, such as "checked" for `input` tags or "selected" for `option` tags,
are "boolean" in the sense that their values don't matter -
it only matters whether or not they're present.
In HTML (but not XHTML), these attributes can be written as

    <input selected>

To do this in Scaml using hash-style attributes, just assign a Scala
`true` value to the attribute:

    %input{:selected => true}

In XHTML, the only valid value for these attributes is the name of the
attribute.  Thus this will render in XHTML as

    <input selected="selected">

To set these attributes to false, simply assign them to a Scala false value.
In both XHTML and HTML

    %input{:selected => false}

will just render as

    <input>

HTML-style boolean attributes can be written just like HTML:

    %input(selected)

or using `true` and `false`:

    %input(selected=true)
-->

### Class and ID: `.` and `#`

The period and pound sign are borrowed from CSS.
They are used as shortcuts to specify the `class`
and `id` attributes of an element, respectively.
Multiple class names can be specified in a similar way to CSS,
by chaining the class names together with periods.
They are placed immediately after the tag and before an attributes hash.
For example:
{pygmentize:: text}
%div#things
  %span#rice Chicken Fried
  %p.beans{ :food => "true" } The magical fruit
  %h1.class.otherclass#id La La La
{pygmentize}

is rendered to:
{pygmentize:: xml}
<div id="things">
  <span id="rice">Chicken Fried</span>
  <p class="beans" food="true">The magical fruit</p>
  <h1 id="id" class="class otherclass">La La La</h1>
</div>
{pygmentize}

And,
{pygmentize:: text}
#content
  .articles
    .article.title Doogie Howser Comes Out
    .article.date 2006-11-05
    .article.entry
      Neil Patrick Harris would like to dispel any rumors that he is straight
{pygmentize}

is rendered to:
{pygmentize:: xml}
<div id="content">
  <div class="articles">
    <div class="article title">Doogie Howser Comes Out</div>
    <div class="article date">2006-11-05</div>
    <div class="article entry">
      Neil Patrick Harris would like to dispel any rumors that he is straight
    </div>
  </div>
</div>
{pygmentize}

#### Implicit Div Elements

Because divs are used so often, they're the default elements.
If you only define a class and/or id using `.` or `#`,
a div is automatically used.
For example:
{pygmentize:: text}
#collection
  .item
    .description What a cool item!
{pygmentize}

is the same as:
{pygmentize:: text}
%div#collection
  %div.item
    %div.description What a cool item!
{pygmentize}

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
{pygmentize:: text}
%br/
%meta{"http-equiv" => "Content-Type", :content => "text/html"}/
{pygmentize}

is rendered to:
{pygmentize:: xml}
<br />
<meta http-equiv="Content-Type" content="text/html" />
{pygmentize}

Some tags are automatically closed, as long as they have no content.
`meta`, `img`, `link`, `script`, `br`, and `hr` tags are closed by default.
This list can be customized by setting the [`ScamlOptions.autoclose`](#autoclose-option) option.
For example:
{pygmentize:: text}
%br
%meta{"http-equiv" => "Content-Type", :content => "text/html"}
{pygmentize}

is also rendered to:
{pygmentize:: xml}
<br/>
<meta http-equiv="Content-Type" content="text/html"/>
{pygmentize}

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
For example:
{pygmentize:: text}
%blockquote<
  %div
    Foo!
{pygmentize}

is rendered to:
{pygmentize:: xml}
<blockquote><div>
  Foo!
</div></blockquote>
{pygmentize}

And:
{pygmentize:: text}
%img
%img>
%img
{pygmentize}

is rendered to:
{pygmentize:: xml}
<img /><img /><img />
{pygmentize}

And:
{pygmentize:: text}
%p<= "Foo\nBar"
{pygmentize}

is rendered to:
{pygmentize:: xml}
<p>Foo
Bar</p>
{pygmentize}

And finally:
{pygmentize:: text}
%img
%pre><
  foo
  bar
%img
{pygmentize}

is rendered to:
{pygmentize:: xml}
<img /><pre>foo
bar</pre><img />
{pygmentize}

## Doctype: `!!!`

When describing HTML documents with Scaml,
you can have a document type or XML prolog generated automatically
by including the characters `!!!`.
For example:
{pygmentize:: text}
!!! XML
!!!
%html
  %head
    %title Myspace
  %body
    %h1 I am the international space station
    %p Sign my guestbook
{pygmentize}

is rendered to:
{pygmentize:: xml}
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
{pygmentize}

You can also specify the specific doctype after the `!!!`
When the [`:format`](#format) is set to `:xhtml` (the default),
the following doctypes are supported:

`!!!`
: XHTML 1.0 Transitional<br/>
 `<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">`

`!!! Strict`
: XHTML 1.0 Strict<br/>
 `<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">`

`!!! Frameset`
: XHTML 1.0 Frameset<br/>
 `<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">`

`!!! 5`
: XHTML 5<br/>
 `<!DOCTYPE html>`<br/>

`!!! 1.1`
: XHTML 1.1<br/>
 `<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">`

`!!! Basic`
: XHTML Basic 1.1<br/>
 `<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML Basic 1.1//EN" "http://www.w3.org/TR/xhtml-basic/xhtml-basic11.dtd"> `

`!!! Mobile`
: XHTML Mobile 1.2<br/>
 `<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.2//EN" "http://www.openmobilealliance.org/tech/DTD/xhtml-mobile12.dtd">`

When the [`:format`](#format) option is set to `:html4`,
the following doctypes are supported:

`!!!`
: HTML 4.01 Transitional<br/>
 `<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">`

`!!! Strict`
: HTML 4.01 Strict<br/>
 `<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">`

`!!! Frameset`
: HTML 4.01 Frameset<br/>
 `<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">`

When the [`:format`](#format) option is set to `:html5`,
`!!!` is always `<!DOCTYPE html>`.

If you're not using the UTF-8 character set for your document,
you can specify which encoding should appear
in the XML prolog in a similar way.
For example:
{pygmentize:: text}
!!! XML iso-8859-1
{pygmentize}

is rendered to:
{pygmentize:: xml}
<?xml version="1.0" encoding="iso-8859-1" ?>
{pygmentize}

## Comments

Scaml supports two sorts of comments:
those that show up in the HTML output
and those that don't.

### HTML Comments: `/`

The forward slash character, when placed at the beginning of a line,
wraps all text after it in an HTML comment.
For example:
{pygmentize:: text}
%peanutbutterjelly
  / This is the peanutbutterjelly element
  I like sandwiches!
{pygmentize}

is rendered to:
{pygmentize:: xml}
<peanutbutterjelly>
  <!-- This is the peanutbutterjelly element -->
  I like sandwiches!
</peanutbutterjelly>
{pygmentize}

The forward slash can also wrap indented sections of code. For example:
{pygmentize:: text}
/
  %p This doesn't render...
  %div
    %h1 Because it's commented out!
{pygmentize}

is rendered to:
{pygmentize:: xml}
<!--
  <p>This doesn't render...</p>
  <div>
    <h1>Because it's commented out!</h1>
  </div>
-->
{pygmentize}

#### Conditional Comments: `/[]`

You can also use [Internet Explorer conditional comments](http://www.quirksmode.org/css/condcom.html)
by enclosing the condition in square brackets after the `/`.
For example:
{pygmentize:: text}
/[if IE]
  %a{ :href => "http://www.mozilla.com/en-US/firefox/" }
    %h1 Get Firefox
{pygmentize}

is rendered to:
{pygmentize:: xml}
<!--[if IE]>
  <a href="http://www.mozilla.com/en-US/firefox/">
    <h1>Get Firefox</h1>
  </a>
<![endif]-->
{pygmentize}

### Scaml Comments: `-#`

The hyphen followed immediately by the pound sign
signifies a silent comment.
Any text following this isn't rendered in the resulting document
at all.

For example:
{pygmentize:: text}
  %p foo
  -# This is a comment
  %p bar
{pygmentize}

is rendered to:
{pygmentize:: xml}
<p>foo</p>
<p>bar</p>
{pygmentize}

You can also nest text beneath a silent comment.
None of this text will be rendered.
For example:
{pygmentize:: text}
%p foo
-#
  This won't be displayed
    Nor will this
%p bar
{pygmentize}

is rendered to:
{pygmentize:: xml}
<p>foo</p>
<p>bar</p>
{pygmentize}

## Scala Evaluation

### Binding Variables `-@`

When a Scalate template is rendered, the caller can pass an attribute map
which the template in charge of rendering. To bind the attribute to a Scala
variable, a Scaml template uses the hyphen character followed by a ampersand 
character and then a scala variable declaration statement.

For example To define an attribute use the following declaration
{pygmentize:: text}
-@ val foo: MyType 
{pygmentize}

If the attribute map does not contain a "foo" entry, then a 
NoValueSetException is thrown when the the template is rendered.

To avoid this exception, a default value can be configured.  For
example:
{pygmentize:: text}
-@ val bar: String = "this is the default value"
{pygmentize}

The attribute is now available for use as an expression. 

Its very common to have a template based on a single object who's members are f
frequently accessed.  In this cases, it's convenient to import all the object's 
members.  This can be done by adding the import keyword to the attribute declaration.

For example:
{pygmentize:: text}
-@ import val model: Person
%p Hello #{name}, what is the weather like in #{city}
{pygmentize}

is the same as:
{pygmentize:: text}
-@ val model: Person
- import model._
%p Hello #{name}, what is the weather like in #{city}
{pygmentize}

Which is the same as:
{pygmentize:: text}
-@ val model: Person
%p Hello #{model.name}, what is the weather like in #{model.city}
{pygmentize}


### Inserting Scala: `=`

The equals character is followed by Scala code.
This code is evaluated and the output is inserted into the document.
For example:
{pygmentize:: text}
%p
  = List("hi", "there", "reader!").mkString(" ")
  = "yo"
{pygmentize}

is compiled to:
{pygmentize:: xml}
<p>
  hi there reader!
  yo
</p>
{pygmentize}

If the [`ScamlOptions.escape_html`](#escape_html-option) option is set, `=` will sanitize any
HTML-sensitive characters generated by the script. For example:
{pygmentize:: text}
= """<script>alert("I'm evil!");</script>"""
{pygmentize}

would be rendered to
{pygmentize:: xml}
&lt;script&gt;alert(&quot;I'm evil!&quot;);&lt;/script&gt;
{pygmentize}

`=` can also be used at the end of a tag to insert Scala code within that tag.
For example:
{pygmentize:: text}
%p= "hello"
{pygmentize}

would be rendered to
{pygmentize:: xml}
<p>hello</p>
{pygmentize}

### Running Scala: `-`

The hyphen character is also followed by Scala code.
This code is evaluated but *not* inserted into the document.

**It is not recommended that you use this widely;
almost all processing code and logic should be restricted
to the Controller, the Helper, or partials.**

For example:
{pygmentize:: text}
- var foo = "hello"
- foo += " there"
- foo += " you!"
%p= foo
{pygmentize}

is rendered to:
{pygmentize:: xml}
<p>hello there you!</p>
{pygmentize}

#### Scala Blocks

Scala blocks, like XHTML tags, don't need to be explicitly closed in Scaml.
Rather, they're automatically closed, based on indentation.
A block begins whenever the indentation is increased
after a Scala insertion or evaluation command.
It ends when the indentation decreases.

<!-- TODO
(as long as it's not an `else` clause or something similar).
-->

For example:
{pygmentize:: text}
- for(i <- 42 to 46)
  %p= i
%p See, I can count!
{pygmentize}

is rendered to:
{pygmentize:: xml}
<p>42</p>
<p>43</p>
<p>44</p>
<p>45</p>
<p>46</p>
<p>See, I can count!</p>
{pygmentize}

Another example:
{pygmentize:: text}
%p
  - 2 match
    - case 1 =>
      = "one"
    - case 2 =>
      = "two"
    - case 3 =>
      = "three"
{pygmentize}

is rendered to:
{pygmentize:: xml}
<p>
  two
</p>
{pygmentize}
    
When inserting evaluated statements, it can also take advantage of Scala blocks. It can be handy
for passing partial functions.  For example:
{pygmentize:: text}
%p
  = List(1,2,3).foldLeft("result: ")
    - (a,x)=>
      - a+x 
{pygmentize}
          
Is the same as:
{pygmentize:: text}
%p
  = List(1,2,3).foldLeft("result: ") { (a,x)=> { a+x } }
{pygmentize}

would be rendered to
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
properly indented.  For example:
{pygmentize:: text}
%html
  %p
    = "line1\nline2\nline3"
{pygmentize}

renders to
{pygmentize:: xml}
<html>
  <p>
    line1
    line2
    line3
  </p>
</html>
{pygmentize}

Sometimes you don't want Scaml to indent the dynamically generated content.
For example, tags like `pre` and `textarea` are whitespace-sensitive;
indenting the text makes them render wrong.

When you use `~` instead of `=`,
Scaml will convert newlines to the XHTML newline escape code, `&#x000A;` and avoid
adding spaces for indentation.  For example:
{pygmentize:: text}
%html
  %pre
    ~ "line1\nline2\nline3"
{pygmentize}

renders to
{pygmentize:: xml}
<html>
  <pre>
    line1&#x000A;line2&#x000A;line3
  </pre>
</html>
{pygmentize}


### Scala Interpolation: `#{}`

Scala code can be interpolated within plain text using `#{}`.
For example:
{pygmentize:: text}
%p This is #{quality} cake!
{pygmentize}

is the same as
{pygmentize:: text}
%p= "This is the "+(quality)+" cake!"
{pygmentize}

and might rendered to
{pygmentize:: xml}
<p>This is scrumptious cake!</p>
{pygmentize}
    
Backslashes can be used to escape `#{` strings,
but they don't act as escapes anywhere else in the string.
For example:
{pygmentize:: text}
%p
  A slash make a difference here: \#{name} is set to: \\#{name}
  But is ignored for: \# or \\
{pygmentize}

might rendered to
{pygmentize:: xml}
<p>
  A slash make a difference here: #{name} is set to: \Hiram
  But is ignored for: \# or \\
</p>
{pygmentize}
    
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
For example:
{pygmentize:: text}
&= "I like cheese & crackers"
{pygmentize}

renders to
{pygmentize:: xml}
I like cheese &amp; crackers
{pygmentize}

If the [`ScamlOptions.escape_html`](#escape_html-option) option is set,
`&=` behaves identically to `=`.

`&` can also be used on its own so that `#{}` interpolation is escaped.
For example,
{pygmentize:: text}
& I like #{"cheese & crackers"}
{pygmentize}

renders to
{pygmentize:: xml}
I like cheese &amp; crackers
{pygmentize}

### Unescaping HTML: `!=` {#unescaping_html}

An exclamation mark followed by one or two equals characters
evaluates Scala code just like the equals would,
but never sanitizes the HTML.

By default, the single equals doesn't sanitize HTML either.
However, if the [`:escape_html`](#escape_html-option) option is set,
`=` will sanitize the HTML, but `!=` still won't.
For example, if `:escape_html` is set:
{pygmentize:: text}
= "I feel <strong>!"
!= "I feel <strong>!"
{pygmentize}

renders to
{pygmentize:: xml}
I feel &lt;strong&gt;!
I feel <strong>!
{pygmentize}

`!` can also be used on its own so that `#{}` interpolation is unescaped.
For example,
{pygmentize:: text}
! I feel #{"<strong>"}!
{pygmentize}

renders to
{pygmentize:: xml}
I feel <strong>!
{pygmentize}

## Filters: `:` {#filters}

The colon character designates a filter.
This allows you to pass an indented block of text as input
to another filtering program and add the result to the output of Haml.

The syntax is a colon followed by an optional list of filter flags and then a colon
separated list of filter names.

A simple example,
{pygmentize:: text}
%p
  :markdown
    Markdown
    ========
    
    Hello, *World*
{pygmentize}

is compiled to
{pygmentize:: xml}
<p>
  <h1>Markdown</h1>

  <p>Hello, <em>World</em></p>
</p>
{pygmentize}

### Filter Interpolation

If you use the `!` or `&` filter flags, you can have Scala code 
interpolated with `#{}` expressions.  It is invalid to use both
the `!` and `&` flags at the same time. 

The `&` flag enables sanitized interpolations.  For example,
{pygmentize:: text}
- var flavor = "<raspberry/>"
#content
  :&markdown
    I *really* prefer #{flavor} jam.
{pygmentize}

is rendered to
{pygmentize:: xml}
<div id="content">
  <p>I <em>really</em> prefer &lt;raspberry/&gt; jam.</p>
</div>
{pygmentize}

The `!` flag enables non-sanitized interpolations.  For example,
{pygmentize:: text}
- var flavor = "<raspberry/>"
#content
  :!markdown
    I *really* prefer #{flavor} jam.
{pygmentize}

is rendered to
{pygmentize:: xml}
<div id="content">
  <p>I <em>really</em> prefer <raspberry/>; jam.</p>
</div>
{pygmentize}

### Filter Whitespace Preservation

The `~` filter flag enables preserves the white space of the content.
The indent level is left unchanged and newlines are converted to `&#x000A;`

For example:
{pygmentize:: text}
%html
  %p<
    :~plain
          Indentation levels are not enforced in filters.
        #{Interpolation} is disabled by default
      Last line
{pygmentize}

is rendered to
{pygmentize:: xml}
<html>
  <p>    Indentation levels are not enforced in filters.&#x000A;  #{Interpolation} is disabled by default&#x000A;Last line</p>
</html>
{pygmentize}

### Filter Chaining

More than one filter can be be used by separating each filter name with a colon.  When
this is done, the filters are chained together so that the output of filter on right, is
passed as input to the filter on the left.  For example:
{pygmentize:: text}
%pre
  :escaped :javascript
    alert("Hello");
{pygmentize}

Is rendered as:
{pygmentize:: xml}
<pre>
  &lt;script type='text/javascript'&gt;
    //&lt;![CDATA[
      alert(&quot;Hello&quot;);
    //]]&gt;
  &lt;/script&gt;
</pre>
{pygmentize}


### Available Filters

Scaml has the following filters defined:

{#plain-filter}
#### `:plain`
Does not parse the filtered text.
This is useful for large blocks of text or HTML.  Really handy when
when you don't want lines starting with `.` or `-` to be parsed.

{#javascript-filter}
#### `:javascript`
Surrounds the filtered text with `<script>` and CDATA tags.
Useful for including inline Javascript.

<!--
{#css-filter}
#### `:css`
Surrounds the filtered text with `<style>` and CDATA tags.
Useful for including inline CSS.

{#cdata-filter}
#### `:cdata`
Surrounds the filtered text with CDATA tags.
-->

{#escaped-filter}
#### `:escaped`
Works the same as plain, but HTML-escapes the text
before placing it in the document.

<!--
{#ruby-filter}
#### `:ruby`
Parses the filtered text with the normal Ruby interpreter.
All output sent to `$stdout`, like with `puts`,
is output into the Haml document.
Not available if the [`:suppress_eval`](#suppress_eval-option) option is set to true.
The Ruby code is evaluated in the same context as the Haml template.

{#erb-filter}
#### `:erb`
Parses the filtered text with ERb, like an RHTML template.
Not available if the [`:suppress_eval`](#suppress_eval-option) option is set to true.
Embedded Ruby code is evaluated in the same context as the Haml template.

{#sass-filter}
#### `:sass`
Parses the filtered text with Sass to produce CSS output.

{#textile-filter}
#### `:textile`
Parses the filtered text with [Textile](http://www.textism.com/tools/textile).
Only works if [RedCloth](http://redcloth.org) is installed.
-->

{#markdown-filter}
#### `:markdown`
Parses the filtered text with [Markdown](http://daringfireball.net/projects/markdown).
Only works if [MarkdownJ](http://markdownj.org/) is found on the class path.

<!--
### Custom Filters

You can also define your own filters. See Haml::Filters for details.
-->

## Other Resources

* [Scalate User Guide](scalate-user-guide.html)

