![Scalate][logo]
===============================

[Scalate 1.4](http://scalate.fusesource.org/blog/releases/release-1-4.html), unreleased
----

* [183](http://scalate.assembla.com/spaces/scalate/tickets/183) switched to [Scala 2.8.1 final release](http://www.scala-lang.org/node/8102) 
* improved the OSGi metadata: optional dependencies are marked optional.
* fixed bugs in the Snippet URL handling
* [#185](http://scalate.assembla.com/spaces/scalate/tickets/185) updated the `{div}` and `{column}` tags evaluation in confluence markup so that they are evaluated as wiki notation
* [#188](http://scalate.assembla.com/spaces/scalate/tickets/188) added support to easily pass in attributes to the site generation step
* added a new maven plugin goal to export confluence sites
* [#189](http://scalate.assembla.com/spaces/scalate/tickets/189) allow the use of HTTP URLs for the snippet source prefix - also default to using pygmentize if its installed unless disabled via Snippets.usePygmentize = false in the scalate.Boot.run() method
* bug fixes in `scalate-wikitext`
* upgrade to ScalaMD version 1.5
* [#190](http://scalate.assembla.com/spaces/scalate/tickets/190) sitegen reports on the template file it failed on
* [#191](http://scalate.assembla.com/spaces/scalate/tickets/191) templates with missing attributes are ignored and a warning is generated
* [#192](http://scalate.assembla.com/spaces/scalate/tickets/192) cache the evaluation of whether pygmentize is installed; which typically doesn't change during an application run
* updated Spring MVC integration: added support for order, prefix, and suffix properties. Removed requirement to use "render:" in view name. Layout render strategy passes the model to the render context.
* [#194](http://scalate.assembla.com/spaces/scalate/tickets/194) added support for a textile filter.
* cleaned up the maven poms so that the scala and logback artifacts are not pushed as transitive dependencies to our users.
* [#195](http://scalate.assembla.com/spaces/scalate/tickets/195) switched to a simpler directory layout for static site generation modules
* added support for using any scalate filter as a macro within markdown.
* [#196](http://scalate.assembla.com/spaces/scalate/tickets/196) added a `scalate create sitegen ...` command to create static sitegen project
* [#197](http://scalate.assembla.com/spaces/scalate/tickets/197) and [#198](http://scalate.assembla.com/spaces/scalate/tickets/198) Option is now treated as a collection of 0 or 1 in Mustache and so that Some(foo) is unwrapped to foo when outputting Option values in any Scalate template language
* [#122](http://scalate.assembla.com/spaces/scalate/tickets/122) allow Mustache templates to layout generated HTML by navigating the 'html' variable to access the head / title or body content.
* [#200](http://scalate.assembla.com/spaces/scalate/tickets/200) moved most log instances to be singleton objects.
* [#204](http://scalate.assembla.com/spaces/scalate/tickets/204) added support a package prefix setting for all generated templates
* [#202](http://scalate.assembla.com/spaces/scalate/tickets/202) added support for the Boot class feature on all TemplateEngines
* [#203](http://scalate.assembla.com/spaces/scalate/tickets/203) moved the sitegen and precompiler core logic into scalate-core so it can be reused by other build tools 
* [#201](http://scalate.assembla.com/spaces/scalate/tickets/201) changed the Maven plugin so that it uses the Scalate version defined in the project's dependency list.
* [#210](http://scalate.assembla.com/spaces/scalate/tickets/210) Fixed template Cache Bug: If scalate can't figure out the last update time of a resource it always considers it stale
* Upgraded to Jersey 1.5
* Upgraded to wikitext 1.2
* [#205](http://scalate.assembla.com/spaces/scalate/tickets/205) Fixed bug where `scalate create` corrupts generated image files on windows
* [#206](http://scalate.assembla.com/spaces/scalate/tickets/206) Fixed bad output generated from the `{children}` confluence macro
* [#207](http://scalate.assembla.com/spaces/scalate/tickets/207) Fixed bug where page titles were not correctly getting set in sitegen project.
* [#208](http://scalate.assembla.com/spaces/scalate/tickets/208) Added CSS and CDATA filters
* [#209](http://scalate.assembla.com/spaces/scalate/tickets/209) Fixed bug where the `{include}` macro does trim the included file name
* [#211](http://scalate.assembla.com/spaces/scalate/tickets/211) Add SBT plugins for the precompiling and sitegen tasks
* [#193](http://scalate.assembla.com/spaces/scalate/tickets/193) Add a :pygmentize filter for use in jade/scaml

[Scalate 1.3.2](http://scalate.fusesource.org/blog/releases/release-1-3-2.html), released 2010-11-24
----

* new [set](http://scalate.fusesource.org/documentation/ssp-reference.html#set) [velocity directive](http://scalate.fusesource.org/documentation/ssp-reference.html#velocity_style_directives) in [Ssp](http://scalate.fusesource.org/documentation/ssp-reference.html#syntax) which lets you assign sections of the template output to attributes so you can more easily pass information into layouts.
* minor refactoring of internal classes such as Resource and ResourceLoader from the org.fusesource.scalate.support package into the org.fusesource.scalate.util package to make the util package more stand alone and reuseable outside of Scalate.
* scalate-util module now refactored out of scalate-core
* both scalate-core and scalate-util now OSGi bundles

For more detail see the [Full Change Log](http://scalate.assembla.com/spaces/scalate/milestones/300141-1-3-1)

[Scalate 1.3.1](http://scalate.fusesource.org/blog/releases/release-1-3-1.html), released 2010-10-27
----

* for folks migrating from Erb, [Ssp](http://scalate.fusesource.org/documentation/ssp-reference.html#syntax) now supports Erb style comments
* [ScalatePackage classes](http://scalate.fusesource.org/documentation/user-guide.html#dry) can now be properly auto-detected for templates which reside in the WEB-INF directory in a web application.
* works inside OSGi containers
* all documentation now correctly included in the distro

For more detail see the [Full Change Log](http://scalate.assembla.com/spaces/scalate/milestones/300141-1-3-1)

[Scalate 1.3](http://scalate.fusesource.org/blog/releases/release-1-3.html), released 2010-10-08
----

* [Jade](http://scalate.fusesource.org/documentation/scaml-reference.html#jade) template syntax is now supported which is a dialect of [Haml](http://haml-lang.com/) or [Scaml](http://scalate.fusesource.org/documentation/scaml-reference.html)
* New [Servlet Filter](http://scalate.fusesource.org/documentation/user-guide.html#using_scalate_as_servlet_filter_in_your_web_application) which allows more flexible mapping of templates in a web application. For example you can have the request */foo.xml* automatically bound to */foo.xml.ssp* if the template exists letting you easily implement views without requiring a controller or routing in your MVC layer. 
* [JSP Converter](http://scalate.fusesource.org/documentation/jspConvert.html) helps you migrate your existing JSP web application across to Scalate
* [HTML Converter](http://scalate.fusesource.org/documentation/htmlConvert.html) lets you migrate your existing HTML files easily to [Scaml](http://scalate.fusesource.org/documentation/scaml-reference.html) or [Jade](http://scalate.fusesource.org/documentation/scaml-reference.html#jade) for extra DRY markup 
* [DRY template imports, values and logic](http://scalate.fusesource.org/documentation/user-guide.html#dry) thanks to Scalate Package objects which allow imports, values and methods to be shared across some or all of your templates to reduce noise inside your templates.
* [Site Generator](http://scalate.fusesource.org/documentation/siteGen.html) lets you generate static or dynamic websites using templates and/or wiki markup together with exporting wiki content from Confluence wikis to migrate to using git/svn as your wiki content repository. You can also use [a common bootstrap approach](http://scalate.fusesource.org/documentation/siteGen.html#bootstrapping) now across both static website generation and web applications - such as to configure wiki macros in a canonical way. We now eat our own dog food and generate this site using Scalate.
* More filters and pipelines supported such as confluence as well as the existing markdown which are particularly useful for website generation (static or semi-static).
* The [Scalate Tool](http://scalate.fusesource.org/documentation/tool.html) now comes with a full interactive shell with full tab completion to make it easier to use the tool either for ad hoc or interactive shell use.

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