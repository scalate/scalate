![Scalate][logo]
===============================


[Scalate 1.3](http://scalate.fusesource.org/blog/releases/release-1-3.html), released 2010-10-08
----

* [Jade](${uri("/documentation/scaml-reference.html#jade")}) template syntax is now supported which is a dialect of [Haml](http://haml-lang.com/) or [Scaml](${uri("/documentation/scaml-reference.html")})
* New [Servlet Filter](${uri("/documentation/user-guide.html#using_scalate_as_servlet_filter_in_your_web_application")}) which allows more flexible mapping of templates in a web application. For example you can have the request */foo.xml* automatically bound to */foo.xml.ssp* if the template exists letting you easily implement views without requiring a controller or routing in your MVC layer. 
* [JSP Converter](${uri("/documentation/jspConvert.html")}) helps you migrate your existing JSP web application across to Scalate
* [HTML Converter](${uri("/documentation/htmlConvert.html")}) lets you migrate your existing HTML files easily to [Scaml](${uri("/documentation/scaml-reference.html")}) or [Jade](${uri("/documentation/scaml-reference.html#jade")}) for extra DRY markup 
* [DRY template imports, values and logic](${uri("/documentation/user-guide.html#dry")}) thanks to Scalate Package objects which allow imports, values and methods to be shared across some or all of your templates to reduce noise inside your templates.
* [Site Generator](${uri("/documentation/siteGen.html")}) lets you generate static or dynamic websites using templates and/or wiki markup together with exporting wiki content from Confluence wikis to migrate to using git/svn as your wiki content repository. You can also use [a common bootstrap approach](${uri("/documentation/siteGen.html#bootstrapping")}) now across both static website generation and web applications - such as to configure wiki macros in a canonical way. We now eat our own dog food and generate this site using Scalate.
* More filters and pipelines supported such as confluence as well as the existing markdown which are particularly useful for website generation (static or semi-static).
* The [Scalate Tool](${uri("/documentation/tool.html")}) now comes with a full interactive shell with full tab completion to make it easier to use the tool either for ad hoc or interactive shell use.

For more detail see the [Full Change Log](http://scalate.assembla.com/spaces/scalate/milestones/208429-1-3)


[Scalate 1.2](http://scalate.fusesource.org/blog/releases/release-1-2.html), released 2010-07-30
----

* Scalate now supports the [Mustache](http://scalate.fusesource.org/documentation/mustache.html) template language which is a Scala dialect of [Mustache](http://mustache.github.com/) for logic-less templates which also work inside the browser using [mustache.js](http://github.com/janl/mustache.js). Support for Mustache uses the same common Scalate API so it works with all the existing Scalate adapters such as servlets, [JAXRS](http://scalate.fusesource.org/documentation/jog.html), [Lift](http://scalate.fusesource.org/documentation/lift.html) or [Play](http://github.com/pk11/play-scalate) and [Apache Camel](http://camel.apache.org/scalate.html)
* Scalate is [now built](http://scalate.assembla.com/spaces/scalate/tickets/70) on top of [Scala 2.8.0 final release](http://www.scala-lang.org/node/7009) 
* [Scuery](http://scalate.fusesource.org/documentation/scuery.html) for jQuery style transformation of HTML or XHTML using CSS3 selectors
* the [console](http://scalate.fusesource.org/documentation/console.html) can be more easily reused in your application [without using WAR overlays](http://scalate.assembla.com/spaces/scalate/tickets/105) and templates can be loaded via the classloader to help make more modular web applications without relying on WAR overlays
* [improvements](http://scalate.assembla.com/spaces/scalate/tickets/94) in associating different template languages to files/URIs/strings/streams in a more flexible API
* [various](http://scalate.assembla.com/spaces/scalate/tickets/108) [improvements](http://scalate.assembla.com/spaces/scalate/tickets/109) in the accuracy of the mapping of scala compiler errors to positions in the template source file which are then shown and linked in the [console](http://scalate.fusesource.org/documentation/console.html)
* improved [maven plugin](http://scalate.fusesource.org/documentation/user-guide.html#precompiling_templates) for precompiling templates

For more detail see the [Full Change Log](http://scalate.assembla.com/spaces/scalate/milestones/191841-1-2)


[Scalate 1.1](http://scalate.fusesource.org/blog/releases/release-1-1.html), released 2010-04-15
----

* [Ssp](http://scalate.fusesource.org/documentation/ssp-reference.html#syntax) now supports [Velocity style directives](http://scalate.fusesource.org/documentation/ssp-reference.html#velocity_style_directives) for more concise looping and branching.
* new [Scalate Tool](http://scalate.fusesource.org/documentation/tool.html) for creating new projects with Scalate more easily
* improved API for working with templates from different sources (file, URL, Source, String etc) via the helper methods on [TemplateSource object](http://scalate.fusesource.org/maven/{project_snapshot_version:}/scalate-core/scaladocs/org/fusesource/scalate/TemplateSource$.html) and methods on [TemplateEngine](http://scalate.fusesource.org/maven/{project_snapshot_version:}/scalate-core/scaladocs/org/fusesource/scalate/TemplateEngine.html) which take a [TemplateSource](http://scalate.fusesource.org/maven/{project_snapshot_version:}/scalate-core/scaladocs/org/fusesource/scalate/TemplateSource.html)
* easier to configure whitespace handling via the **escapeMarkup** property on [TemplateEngine](http://scalate.fusesource.org/maven/{project_snapshot_version:}/scalate-core/scaladocs/org/fusesource/scalate/TemplateEngine.html) and [RenderContext](http://scalate.fusesource.org/maven/{project_snapshot_version:}/scalate-core/scaladocs/org/fusesource/scalate/RenderContext.html) so its easy to configure markup escaping for an entire project or enable/disable it within templates.

For more detail see the [Full Change Log](http://scalate.assembla.com/spaces/scalate/milestones/191837-1-1)


[Scalate 1.0](http://scalate.fusesource.org/blog/releases/2010/04/release-1-0.html), released 2010-04-06
----

Initial release with support for the following template languages

* [Ssp](http://scalate.fusesource.org/documentation/ssp-reference.html#syntax) which is like a Scala version of JSP or Erb from Rails 
* [Scaml](http://scalate.fusesource.org/documentation/scaml-reference.html) which is a Scala dialect of [Haml](http://haml-lang.com/) for very DRY markup

[logo]: http://scalate.fusesource.org/images/project-logo.png "Scalate"