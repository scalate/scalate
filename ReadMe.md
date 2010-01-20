SCALA SERVER PAGES
==================

This library implements Scala Server Pages which are [Scala](http://www.scala-lang.org) versions of things like JSP, ASP, GSP.

The code is based on code by [Matt Hildebrand](http://github.com/matthild/serverpages)


Syntax
------

Expressions:

	<%= ... %>
	${ ... }    <-- this simply HTML-escapes the result of the Scala expression "..."

Declarations:
	<% ... %>

Imports:
	<%@ include file="relativeURL" %>


There is no support for tag libraries, EL, or anything like JSTL.

Requires a Java 5 runtime and a servlet 2.5 container, or newer.


Building
--------

Install [maven](http://maven.apache.org) version 2.0.9 or later. Then type

    mvn

To run the sample web application

    cd ssp-sample
    mvn jetty:run

Then open http://localhost:8080


ADDING SSP SUPPORT TO YOUR APPLICATION
======================================

1.  Add something like the following to your web.xml file:

        <servlet>
          <servlet-name>SspServlet</servlet-name>
          <servlet-class>com.mh.serverpages.scala_.ScalaServerPageServlet</servlet-class>
          <load-on-startup>1</load-on-startup>
        </servlet>

        <servlet-mapping>
          <servlet-name>SspServlet</servlet-name>
          <url-pattern>*.ssp</url-pattern>
        </servlet-mapping>

2.  Include the following JARs in your servlet's runtime environment (probably in WEB-INF/lib):

        scala-compiler.jar
        scala-library.jar
        ssp-core.jar

You could add one or more of the above to your servlet container's server-wide configuration if you prefer


Possible Gotchas
----------------

- Works with expanded WARs - or servlet containers who's ClassLoader implements URLClassLoader

- Assumes SSP page source files are all UTF-8-encoded.

- Assumes SSP pages' output is all UTF-8-encoded.

- No support for precompilation (e.g., via a custom Ant task).

- Yes, there really is an underscore in "com.mh.serverpages.scala_.ScalaServerPageServlet"; the Scala compiler issued errors without the underscore in the package name.


Changes
-------
* added [Maven](http://maven.apache.org) build and experimental [sbt](http://code.google.com/p/simple-build-tool/) build
* fixed issue where SSPs would not be recompiled if an error occurred compiling between application restarts
* fixed so it works in jetty-run and other servlet contexts; so we can reuse the ClassLoader if its a URLClassLoader
* added a PageContext so we can add helper methods like this to look up attributes in a typesafe way

    val foo = attribute[Cheese]("whatnot")
    val bar = attributeOrElse("blah", someDefaultValue)
