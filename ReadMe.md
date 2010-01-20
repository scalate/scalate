Scala Server Pages
==================

This library implements Scala Server Pages which are Scala versions of things like JSP, ASP, GSP.

The code is based on code by [Matt Hildebrand](http://github.com/matthild/serverpages)

Changes
-------
* added [Maven](http://maven.apache.org) build and experimental [sbt](http://code.google.com/p/simple-build-tool/) build
* fixed issue where SSPs would not be recompiled if an error occurred compiling between application restarts
* fixed so it works in jetty-run and other servlet contexts; so we can reuse the ClassLoader if its a URLClassLoader
* added a PageContext so we can add helper methods like this to look up attributes in a typesafe way
    val foo = attribute[Cheese]("whatnot")
    val bar = attributeOrElse("blah", someDefaultValue)
