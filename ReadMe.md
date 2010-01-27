ScalaTE (Scala Template Engine)
===============================

This library implements ScalaTE which are [Scala](http://www.scala-lang.org) versions of things like JSP and HAML.

All expressions inside ScalaTE are typesafe and checked at edit/compile time to ensure you don't leave any mistakes in your pages.

Two template languages are supported:
  * SSP: Provides a JSP, ASP, ERB style template language
  * HAML: Provides Haml style template lanaguage

The code is based on code by [Matt Hildebrand](http://github.com/matthild/serverpages)

Requirements
------------

* Java 5 runtime or newer.
* Scala 2.8
* Servlet 2.5 container or newer.

SSP Syntax
----------

SSP Expressions:

	<%= ... %>
	${ ... }    <-- this simply HTML-escapes the result of the Scala expression "..."

Declarations:
	<% ... %>

Imports:
	<%@ include file="relativeURL" %>

There is no support for tag libraries, EL, or anything like JSTL.

Variables
---------

The following variables are in scope inside your page

<table>
  <tr>
    <th>Variable & Type</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>pageContext : PageContext</td>
    <td>the page context which is imported to provide helper methods like attribute() as described belowt</td>
  </tr>
  <tr>
    <td>out : PrintWriter</td>
    <td>the printer of the output</td>
  </tr>
  <tr>
    <td>request: HttpServletRequest</td>
    <td>the servlet request</td>
  </tr>
  <tr>
    <td>response: HttpServletResponse</td>
    <td>the servlet response</td>
  </tr>
</table>

Working with attributes
-----------------------

You often want to bind to attributes registered in the servlet, controller, JAXRS Resource bean.
Here's how you can do that in a typesafe way

    val foo = attribute[Cheese]("blah") // throws exception if not available

    val bar = attributeOrElse("bar", "someDefaultValueExpressionHere") // uses default if not present

You can reuse all the power of scala in your expressions. So if you have a controller or attribute available
you may wish to import its fields & methods so you can use them in your SSP file.

    <% val controller = attribute[MyController]("someName"); import controller._ %>


Working with JAX-RS
-------------------

When working with the [Jersey](https://jersey.dev.java.net/) implementation of JAX-RS you can use template engines as implicit views of your resources.

To do this just add the @ImplicitProduces annotation to your resource bean, then create a SSP file called "index.ssp"
in a directory named after the fully qualified resource class name.

For example if your resource bean is called org.fusesource.scalate.sample.resources.FooResource then create a file in your webapp called
com/mh/serverpages/sample/resources/FooResource/index.ssp

In your SSP you can refer to the resource bean as follows

    <% val it = resource[FooResource] %>

You often want to import all the methods and fields from the resource bean so you can do this

    <% val it = resource[FooResource]; import it._ %>
    
Haml Syntax
-----------

* TODO

Building
--------

Install [maven](http://maven.apache.org) version 2.0.9 or later. Then type

    mvn

To run the sample web application

    cd scalate-sample
    mvn jetty:run

Then open [the sample home page](http://localhost:8080)


ADDING SSP SUPPORT TO YOUR APPLICATION
======================================

1.  Add something like the following to your web.xml file:

        <servlet>
          <servlet-name>SspServlet</servlet-name>
          <servlet-class>org.fusesource.scalate.scala_.ScalaServerPageServlet</servlet-class>
          <load-on-startup>1</load-on-startup>
        </servlet>

        <servlet-mapping>
          <servlet-name>SspServlet</servlet-name>
          <url-pattern>*.ssp</url-pattern>
        </servlet-mapping>

2.  Include the following JARs in your servlet's runtime environment (probably in WEB-INF/lib):

        scala-compiler.jar
        scala-library.jar
        scalate-core.jar

You could add one or more of the above to your servlet container's server-wide configuration if you prefer


Possible Gotchas
----------------

- Works with expanded WARs - or servlet containers who's ClassLoader implements URLClassLoader

- Assumes SSP page source files are all UTF-8-encoded.

- Assumes SSP pages' output is all UTF-8-encoded.

- No support for precompilation (e.g., via a custom Ant task).

- Yes, there really is an underscore in "org.fusesource.scalate.scala_.ScalaServerPageServlet"; the Scala compiler issued errors without the underscore in the package name.


Changes
-------
* added [Maven](http://maven.apache.org) build and experimental [sbt](http://code.google.com/p/simple-build-tool/) build
* fixed issue where SSPs would not be recompiled if an error occurred compiling between application restarts
* fixed so it works in jetty-run and other servlet contexts; so we can reuse the ClassLoader if its a URLClassLoader
* added a PageContext so we can add helper methods like this to look up attributes or resources beans in a typesafe way
* updated the name mangling so that when using SSP views for JAXRS resource beans there's no need to import the package of the resource bean (as the SSP is generated in a child package)
* markup using Scala's Nodes are not XML encoded so they can be used to create markup
* numbers and dates use the request's locale by default to format themselves
