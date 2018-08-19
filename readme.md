![Scalate][logo]
===============================

[![Build Status](https://travis-ci.org/scalate/scalate.svg?branch=master)](https://travis-ci.org/scalate/scalate)
[![Maven Central](https://img.shields.io/maven-central/v/org.scalatra.scalate/scalate-core_2.12.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.scalatra.scalate%22%20a%3A%22scalate-core_2.12%22)

Scalate is a [Scala](http://www.scala-lang.org) based template engine which supports HAML, Mustache and JSP, Erb and Velocity style syntaxes.

The following template languages are supported:

  * [Mustache](http://scalate.github.io/scalate/documentation/mustache.html#features): is a Mustache template language for Java/Scala
  * [Jade](http://scalate.github.io/scalate/documentation/scaml-reference.html#jade): The Jade style of Haml/Scaml template language
  * [Scaml](http://scalate.github.io/scalate/documentation/scaml-reference.html#features): Provides Haml style template language
  * [Ssp](http://scalate.github.io/scalate/documentation/ssp-reference.html#syntax): Provides a JSP, Erb and Velocity style template language

In Scaml and SSP all expressions are typesafe and checked at edit/compile time to ensure you don't leave any mistakes in your pages.
Mustache uses dynamic typing and reflection; a trade off of hiding code and logic from inside the templates.

Building
--------

To build Scalate from the source please see the [building instructions](http://scalate.github.io/scalate/building.html)

Links
-----

* [Home](http://scalate.github.io/scalate)
* [Community](http://scalate.github.io/scalate/community.html)
* [Documentation](http://scalate.github.io/scalate/documentation/)
* [Issue Tracker](https://github.com/scalate/scalate/issues)

[logo]: http://scalate.github.io/scalate/images/project-logo.png "Scalate"
