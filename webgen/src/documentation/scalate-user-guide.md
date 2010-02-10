# Scalate User Guide

* Table of contents
{:toc}

Scalate is a template engine based on the Scala language.

## Features

* Supports multiple template syntaxes
  * Ssp: like JSP/ASP pages in syntax but using Scala code for expressions
  * Scaml: like [Haml](http://haml-lang.com/) pages in syntax, but again with Scala as the expression language.
* Easy to use replacement for JSP's in J2EE web container
* No hard dependencies on a web container.  It can be used in a standalone application to template things like emails.
* JAXRS integration so that Scalate template can render JAXRS resouces

## Ssp (Scala Server Pages) Introduction

If you know JSP or ASP then hopefully the syntax of Ssp is familiar; only using Scala as the language of expressions and method invocations.

Example:

{pygmentize:: jsp}
<%@ var user:User %>
<p>Hi ${user.name},</p>
<% for(i <- 1 to 3) { %>
<p><%= i %></p>
<% } %>
<p>See, I can count!</p>
{pygmentize}

Is rendered as:

{pygmentize:: xml}
<p>Hi James,</p>
<p>1</p>
<p>2</p>
<p>3</p>
<p>See, I can count!</p>
{pygmentize}

For full documentation of the Ssp syntax see the [Ssp Reference Guide](ssp-reference.html)


## Scaml (Scala Markup Language) Introduction

Scaml is a markup language
that's used to cleanly and simply describe the XHTML of any web document,
without the use of inline code.  It is Scala version of
[Haml](http://haml-lang.com/).
Scaml functions as a replacement
for inline page templating systems such as PHP, ERB, and ASP.
However, Scaml avoids the need for explicitly coding XHTML into the template,
because it is actually an abstract description of the XHTML,
with some code to generate dynamic content.

Example :

{pygmentize:: text}
-@ var user:User
%p Hi #{user.name},
- for(i <- 1 to 3)
  %p= i
%p See, I can count!
{pygmentize}

Is rendered as:

{pygmentize:: xml}
<p>Hi James,</p>
<p>1</p>
<p>2</p>
<p>3</p>
<p>See, I can count!</p>
{pygmentize}

For full documentation of the Scaml syntax see the [Scaml Reference Guide](scaml-reference.html)


## Requirements

General Requirements:

* Java 5 runtime or Newer
* Scala 2.8 

Web Container Requirements:

* Servlet 2.5 container or Newer

## Building From Source

Scalate can be built either using Maven or SBT

### Using Maven

Install [Maven](http://maven.apache.org/) version 2.0.9 or later. Then type

    mvn install
{: .syntax }

To run the sample web application

    cd scalate-sample
    mvn jetty:run
{: .syntax }

Then open [the sample home page](http://localhost:8080/)

### Using SBT

You can also use [sbt](http://code.google.com/p/simple-build-tool/ "simple build tool") to build Scalate.

To setup your sbt environment and import the dependencies from the maven pom files type

    ./sbt
    update
{: .syntax }

Then to build the code type

    compile
{: .syntax }

to run the tests 

    test
{: .syntax }

For more information see the [sbt building instructions](http://scalate.fusesource.org/sbt.html)

## Using Scalate in your Web Application


* Add something like the following to your web.xml file to support Ssp and Scaml pages:

  {pygmentize:: {lang: xml, lines: true}}
  <servlet>
    <servlet-name>TemplateEngineServlet</servlet-name>
    <servlet-class>org.fusesource.scalate.servlet.TemplateEngineServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
  </servlet>

  <servlet-mapping>
    <servlet-name>TemplateEngineServlet</servlet-name>
    <url-pattern>*.ssp</url-pattern>
  </servlet-mapping>
  <servlet-mapping>
    <servlet-name>TemplateEngineServlet</servlet-name>
    <url-pattern>*.scaml</url-pattern>
  </servlet-mapping>
  {pygmentize}

*  Include the following JARs in your servlet's runtime environment (probably in WEB-INF/lib):
    * scala-compiler.jar
    * scala-library.jar
    * scalate-core.jar

You could add one or more of the above to your servlet container's server-wide configuration if you prefer

### Possible Gotchas

- Works with expanded WARs - or servlet containers who's ClassLoader implements URLClassLoader

- Assumes template source files are all UTF-8-encoded.

- Assumes template output is all UTF-8-encoded.

- No support for pre-compiled templates (e.g., via a custom Ant task).

## Embedding Scalate in your Application or Framework

Scalate does not have any hard dependencies on a web framework or even HTTP.  It can be used as a standalone
rendering engine in your application.  For more information on how to embed in your application, please reference the 
[Scalate Embedding Guide](scalate-embedding-guide.html)

