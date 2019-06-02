![Scalate][logo]
===============================
git log --pretty=format:'%h %s by %an' --abbrev-commit | grep -v "Merge pull request " | head -50

[Scalate 1.9.4 (WIP)](https://github.com/scalate/scalate/compare/scalate-project-1.9.3...scalate-project-1.9.4), released TBD

* 123fbddb Scala 2.13.0-RC3 by xuwei-k
* 2d1b96ce Update spring-webmvc to 5.1.7.RELEASE by xuwei-k

[Scalate 1.9.3](https://github.com/scalate/scalate/compare/scalate-project-1.9.2...scalate-project-1.9.3), released 2019-04-13

* e1c246b6 Fix parser-combinators bin-compatibility issue in Scala 2.11 by Kazuhiro Sera
* b9d841f7 Update jruby-complete to 9.2.7.0 by xuwei-k

[Scalate 1.9.2](https://github.com/scalate/scalate/compare/scalate-project-1.9.1...scalate-project-1.9.2), released 2019-04-08

* 4e3c7f30 Support Scala 2.13.0-RC1 by Kazuhiro Sera
* 3a6bee40 Update spring-webmvc to 5.1.6.RELEASE by xuwei-k
* 696cb5d2 Update dependencies by xuwei-k
* 3c55167b update sbt plugins by xuwei-k

[Scalate 1.9.1](https://github.com/scalate/scalate/compare/scalate-project-1.9.0...scalate-project-1.9.1), released 2019-03-06

* 73003700 Use StringBuffer.append(char) instead of StringBuffer.append(Object) for regular characters. Avoid Char boxing and Character.toString calls.
* 38a92d89 Support Scala 2.13.0-M5
* 22a49cac Removed unused repositories
* 2bd5ab63 org.apache.felix.gogo.commands* are deprecated
* 71c208de method inUltimateSource is deprecated
* 4160f2d7 Class#newInstance is deprecated since Java9
* 424c0212 Class#newInstance is deprecated since Java9
* a318a00d Fix typos and misspellings #185
* c7e0ad44 Fixed test code for both pygmentize 1.6 and 2.2
* a1764797 enabled pygmentize's test on Travis-CI

[Scalate 1.9.0](https://github.com/scalate/scalate/compare/scalate-project-1.8.0...scalate-project-1.9.0), released 2018-07-19

* Move jaxrs and jersey related classes to separate modules
* Fix build for jaxrs and jersey modules
* Remove Scalate Tool (no longer maintained)
* Work needed for the migration from the deprecated sbt.Plugin
* Remove unused imports
* Fix the code which unnecessarily uses var instead of val
* Remvoe unused variables
* Migrate to sbt 1.x
* Bump dependencies
* Fix "Adapting argument list" warnings
* Bump scalamd to 1.7.1
* Use classpath#distinc instead of Sequences.removeDuplicates
* Use scala.collection.Seq instead of scala.Seq
* Replace TraversableForwarder usages because the module will be removed in Scala 2.13
* Remove procedure syntax usages
* Remove unused private fields
* Remove unnecessary "return"
* Use "HashMap.empty" instead of "new HashMap"
* Fix JavaConverters usages which will be removed in Scala 2.13
* Many necessary changes for Scala 2.13 adoption (thanks to Kenji Yoshida)
* Bump scala-parser-combinators to 1.1.1
* Remove unused local default argument
* Upgrade JRuby version to the latest
* Add MiMa detection policies

[Scalate 1.8.0](https://github.com/scalate/scalate/compare/scalate-project_2.11-1.7.1...scalate-project-1.8.0), released 2016-11-05

* Fix BundleClassPathBuilder broken switching from 2.11 to 2.10 base branch
* Provide 'generate-scala' and 'generate-website' tool commands
* Enable Travis build
* Add sbt-scalariform to sbt settings + Apply scalariform without manual modifications
* Fix most of the existing warnings on source code
* Enable to compile for Scala 2.12.0
* Bump dependencies

[Scalate 1.7.1](https://github.com/scalate/scalate/compare/scalate-project_2.11-1.7.0...scalate-project_2.11-1.7.1), released 2015-03-16

* Update of documentation contribution page (site.page)
* Link fixes and updated comments in the development and build pages
* Edit the User Guide documentation to clarify a few items
* Grammar fix - subject-verb agreement
* Add thread-safe RW access of ObjectIntrospector
* Remove odd maybeCache

[Scalate 1.7.0](https://github.com/scalate/scalate/compare/scalate-project_2.10-1.6.1...scalate-project_2.11-1.7.0), released 2014-05-03

* Fixed distro and tool for 2.10
* Fix an issue where Spring MVC does not work when there is no ServletConfig
* Replace Rhino 1.7R2 with RhinoCoffeeScript
* Fix the problem a SourceMap is not correctly generated when a compiled .scala has large string literal(> 32276)
* Upgrade to lesscss 1.3.3
* Enable server side includes in less processing, by providing a lesscss ResourceLoader
* Enable server side includes into less snippets embedded in HTML
* Use RenderContext instead of DefaultRenderContext
* Add the scala-parser-combinators module

[Scalate 1.6.1](http://scalate.fusesource.org/blog/releases/release-1.6.1.html), released 2012-12-29

* Make sure Java 1.6 is targeted (1.6 release only worked with 1.7)

[Scalate 1.6](http://scalate.fusesource.org/blog/releases/release-1.6.0.html), released 2012-12-21
----

* Built against scala 2.10
* support stripping newlines from output in SSP templates
* Test that consecutive Jade includes work.
* unexpected fragment in parse results: improved error reporting, empty string is emitted instead of "()" - representation of Unit returned from println
* use zinc server for incremental compilation when available
* java.io.File.toUrl is deprecated
* Predef.error is deprecated in Scala 2.10
* Applications should never catch subclasses of java.lang.Error
* Remove unused scala-mojo-support from POM.
* Precompiled template loading was broken.  If the compiler is not installed, throws a ResourceNotFoundException instead of a TemplateException since we don't want to short circuit attempting to load other templates which might be precompiled.
* When Global is not on the classpath, a Throwable is thrown, not an Exception.
* disable maven.glassfish.org (nexus is now maven.java.net), there's some transitive dependency calling this that causes problems for maven-scalate-plugin if you don't have it cached locally
* Fix method name used in reflection.
* convert to mixed java/scala module so we can use the standard Maven Java Mojo extractors.  Drop use the the scala-mojo support.
* It's a jar not a bundle.
* Fix maven-compat version.
* Clean up pom so that build works.
* Fix bad group ID on scalatra-mojo-support.
* scalamd has been published upstream, roll back to it.
* Use public versions of all plugins; now builds clean with -Pdownload.
* Removed pure expression in statement position, according to scalac warning.
* Hide Tuple style accessors _1, _2 etc because IntrospectorTest does not expect to see them. The test failure could be fixed the other way around, but my feeling is that named properties are more useful inside a template than numbered ones.
* Adjusted to scala.tools.nsc.io.AbstractFile API change
* Fixed "missing parameter type" error.
* scalate-util module declares packaging type bundle. Update dependency coordinate to match that.
* Update artifactIDs on website with Scala binary compatibility tag.
* Vainly add self to pom.
* Add pooling for pegdown
* Add a test for pegdown filtering
* Fix package name in pegdown addon.index
* Make Pegdown filter a class so extensions can be configured
* Clean up pom so that build works.
* Pickup some wikitext snapshot changes.
* Fix bad group ID on scalatra-mojo-support.
* Reset the Scala Compiler between runs.
* Append _${scala-version} tag to all artifact IDs to denote Scala binary compatibility.
* Don't depend on backout after fatal errors, it's a Scala bug.
* scalamd has been published upstream, roll back to it.
* Use public versions of all plugins; now builds clean with -Pdownload.
* Upgrade to scala-maven-plugin 3.1.0 for incremental compilation.
* Revert "Enable commented LoopTest."
* Make scalate-archetypes-itests versions match.
* Move addon filters into their own subpackages
* Enable commented LoopTest.
* Scala Tools is dead. Long live Sonatype.
* Changes to support OSGi with 2.10.0
* Discarding exception: let all subclasses of Error pass unaffected. When a class is found on the classpath but could not be loaded due to an LinkageError, the developer needs to know about this.
* Discarding caught exception: let subclasses of Error pass unaffected
* Rethrowing exception: use explicit Throwable type ascription to pacify the compiler
* Exception wrapping: let VirtualMachineError and ThreadDeath pass unaffected
* Removed redundant try/catch
* updated mvnplugins to 1.27-SNAPSHOT, awaiting Scala 2.10 compatible release
* Removed pure expression in statement position, according to scalac warning.
* Fixed a deprecation warning: pass on minimizeEmpty property of Element being processed
* Hide Tuple style accessors _1, _2 etc because IntrospectorTest does not expect to see them. The test failure could be fixed the other way around, but my feeling is that named properties are more useful inside a template than numbered ones.
* Fixed some deprecation warnings in the compiler
* Scala 2.10: scala.reflect.ClassTag instead of scala.reflect.Manifest
* Removed explicit adding of scala-library, scala-compiler and scalate-core bundles to compiler classpath. Client bundle needs to import relevant packages anyway to load the classes later, so the necessary bundles will be added by ClassPathBuilder.fromBundle.
* Add bundle to classpath only once irregardles of the number of imported packages. LinkedHashSet is used to preserve correct order.
* Override Global.classPath to our generated classpath.
* Do not create ServletTemplateEngine for every view, reuse once created one. Invoke boot class.
* Switched from presentation compiler to regular one to get around bytecode generation problem. See: https://gist.github.com/3488314
* Adjust to scala compiler API change.
* Adjusted to scala.tools.nsc.io.AbstractFile API change
* Commented out a test that breaks Scala compiler. See: https://gist.github.com/3488124
* Fixed "missing parameter type" error.
* scalate-util module declares packaging type bundle. Update dependency coordinate to match that.
* Update scala version to 2.10.0-M7, scalate-test to 1.9-2.10.0-M7-B1
* Treat None case with and without parents the same in invertedSection
* Upgrade to latest jruby
* Have ScalateView properly register its ServletRenderContext objects
* Change jrebel maven repo as the older does not longer exist, add option <updatePolicy> = always
* Add parens to effectful shutdown methods.
* Add a shutdown method on Compiler
* Now that TemplateEngine uses a ScalaCompiler, it needs a method for shutting it down.
* Scaml declarations use at signs, not ampersands. Thanks, josiahg.
* Update 500.scaml to import scala.util.parsing.input.Position by full name instead of relative package.
* Avoid duplicate TemplateSource reads unless using pipeline
* Reset scala Source before read to support multiple reads
* Do not retry template compilation twice
* Use buildhelper plugin to include src/main/scala as source dir
* Fixed {children} macro to work with static web sites.
* Switching to the 1.4-SNAPSHOT release of wikitext
* [#120] Made some classloader changes for running in an OSGi environment.
* Refactored ConfExport Mojo so that it re-uses code from scalate-tool.
* Add support for sass/scss @import
* Comment out the support bits as it's not official.
* Added Less Filter and attempted to modify the sbt and Maven builds to match this and the previous Pegdown filter.
* Added PegDown filter
* selective scuery attribute updates (update attribute if it exists)
* Fixed bug introduced in last commit - removing org.apache.axis:axis-jaxrpc dependency from POM
* Updated assembly descriptor for refactored confexport command
* Upgraded axis-wsdl4j dependency from 1.2 to 1.5.1
* Added --target-db switch to generate DocBook link database.
* Refactored confexport command to use the libraries from the Confluence Command Line Interface (CLI).
* updated site to redeploy to main home page

[Scalate 1.5.3](http://scalate.fusesource.org/blog/releases/release-1.5.3.html), released 2011-11-14
----

* Scalate now uses the [Scala Presentation Compiler](https://groups.google.com/d/msg/scalate/3mrkmrXK7vs/7nBh96DPT4YJ) to boost performance of template compilation 3-10X
* Support for compiling stand alone CoffeeScript files on the server, CoffeeScript filters and various CoffeeScript related bug fixes 
* A [pure Java API](http://www.assembla.com/spaces/scalate/tickets/129) to working with Scalate

[Scalate 1.5.2](http://scalate.fusesource.org/blog/releases/release-1.5.2.html), released 2011-09-09
----

* Server side compilation of CoffeeScript in the [:coffeescript filter](http://scalate.fusesource.org/documentation/jade-syntax.html#filters) - many thanks for the [patch](https://github.com/scalate/scalate/pull/6)
* Provide a Scala 2.8.1 distribution of Scalate too (version **1.5.2-scala_2.8.1**) for easier [Play](http://www.playframework.org/) integration and working with other Scala 2.8.x projects.
* Minor improvements in the use of the ScalaCompiler to make it easier to support [Lifty](http://lifty.github.com/) as a plugin inside [SBT](https://github.com/harrah/xsbt/wiki) - thanks for the help and welcome to the team [Mads](https://github.com/mads379)
* Fixed [#260](http://scalate.assembla.com/spaces/scalate/tickets/260) : Scalate distro does not include all the jars required for textile support

[Scalate 1.5.1](http://scalate.fusesource.org/blog/releases/release-1.5.1.html), released 2011-08-08
----

* Fixes [#252](http://scalate.assembla.com/spaces/scalate/tickets/252) : Maven sitegen goal should set the work directory
* Fixes [#251](http://scalate.assembla.com/spaces/scalate/tickets/251) : Dynamically generated template sources should be stored under the scalate working directory and avoid using package declarations.
* Fixes failing Sass test.
* updated to a recent camel release
* added a camel page describing the scalate-camel component
* added an ExpressionTag so its easy to make new confluence tags using a scala function, such as for {project_version}
* fixed the Sass filter to wrap it in the <style> element like the CssFilter - and added a test case
* added missing pages from demo :)
* updated docs to refer to HTML 5 headers for jade/scaml
* added missing pages from demo :)
* fixed index page
* Merge branch 'master' of github.com:scalate/scalate
* added a little sample to kinda showcase how layouts work and how the different template languages look and feel
* fixes [#242494](http://scalate.assembla.com/spaces/scalate/tickets/249) to migrate the default archetypes over to using jade
* fix for loading precompiled templates
* tried a better fix for the NPE issue :)
* fixed possible NPE
* Fix link.
* remove unneeded file.

[Scalate 1.5.0](http://scalate.fusesource.org/blog/releases/release-1.5.0.html), released 2011-06-01
----

* Fixes[#244](http://scalate.assembla.com/spaces/scalate/tickets/244) Error Page Template not display if precompiled and source excluded from webapp
* Try to load the source's content early in compileAndLoad so that a ResourceNotFoundException throw before a TemplateException due to the scala compiler not being available.
* Fixes[#243](http://scalate.assembla.com/spaces/scalate/tickets/243)  Updated to Scala 2.9.0-1
* Fixes[#242](http://scalate.assembla.com/spaces/scalate/tickets/242) to add simple helper methods to turn measurement units into nice pretty strings
* added helper method to load a template as text such as to render a jade template as source inside a template for client side rendering
* support easy access to lazy created sets/lists/maps in the attributes.
* Fixes[#239](http://scalate.assembla.com/spaces/scalate/tickets/239) : Adding a cofeescript filter.
* Fixes[#238](http://scalate.assembla.com/spaces/scalate/tickets/238): scaml/jade using = on one line doesn't like a space before the =
* fixes [#235](http://scalate.assembla.com/spaces/scalate/tickets/235) so that jsp2ssp is now available as a tool
* added new captureAttributeAppend method which fixes [#230](http://scalate.assembla.com/spaces/scalate/tickets/230)
* Fix classpath for scala compiler in osgi
* fixed up documentation bug
* explicitly reset the test counter just in case
* added test case to check we can implement a Boot class in pure Java easily
* moved the jrebel dependency repositories into the download profile and added more docs to the website
* added test case for Wille's issue: http://groups.google.com/group/scalate/browse_thread/thread/78013156e89b1ee8
* added a sample to test out the use of precompiling templates
* fixes [#228](http://scalate.assembla.com/spaces/scalate/tickets/228) to provide a JRebel plugin for Scalate so that templates are reloaded whenever JRebel reloads a class. Its pretty pessimistic so far; we should be able to minimise the reloading of templates using JRebel's dependency tracking
* Add doco that the scss and sass filters are available.
* Trimming files form the haml distro that are not needed at runtime.
* Fixes [#227](http://scalate.assembla.com/spaces/scalate/tickets/227) : Added scss and sass filters!
* Use the right javadoc annotation style.
* removed some unnecessary dependencies from poms
* made scala-compiler a default dependency so that mvn jetty:run and mvn tomcat:run work fine; folks can always exclude the dependency or specify it a provided scope dependency if they want to exclude it from a WAR
* Update the description of the from parameter since it can be a HTTP url too now.
* omit the div declaration when a class or style attribute is available.
* Fixes [#225](http://scalate.assembla.com/spaces/scalate/tickets/225) We now check to see if the scala compiler is installed and disable template reloading if it's not.  Also print a more descriptive message if we HAVE to compile a template and it's not available.
* Run the html through the tidy command if it's available, strip the doctype header, and handle multi line text areas properly.
* fixes [#223](http://scalate.assembla.com/spaces/scalate/tickets/223) Use CSS comments to hide the CDATA expressions.
* Merge branch 'master' of github.com:scalate/scalate
* support dynamic attribute values in the ruby style attribute syntax in jade & scaml. fixes [#222](http://scalate.assembla.com/spaces/scalate/tickets/222)
* Fixes NPE that occurs when generateScala is called.
* fixed up the user guide a bit more to mention the DRY IT approach


[Scalate 1.4.1](http://scalate.fusesource.org/blog/releases/release-1-4-1.html), released 2011-02-25
----

* Fixes [#219](http://scalate.assembla.com/spaces/scalate/tickets/219) removes the error attributes from the request context if scalate directly rendered the error page.
* Fixes [#221](http://scalate.assembla.com/spaces/scalate/tickets/221) to add a scalate-web dependency and use it in scalate-war for simplicity
* Added scala-library dependency to scalate-util; if you want to exclude this dependency due to different scala versions you can add an exclusion easily. scala-compiler is an optional dependency on scala-core now
* Fixes [#220](http://scalate.assembla.com/spaces/scalate/tickets/220) so that we can use tomcat:run inside archetypes and projects inside scalate
* Fixes to the spring mvc integration to make the contentType works
* Fixes scalate core so it can run on Java 1.5 once again
* Fixes [#216](http://scalate.assembla.com/spaces/scalate/tickets/216) to let users properly override the number format
* Fixes [#199](http://scalate.assembla.com/spaces/scalate/tickets/199) to treat java collections and Maps better in mustache
* Fixes SBT pre-compiler and sitegen plugins

[Scalate 1.4](http://scalate.fusesource.org/blog/releases/release-1-4-0.html), released 2011-02-10
----

* [#183](http://scalate.assembla.com/spaces/scalate/tickets/183) switched to [Scala 2.8.1 final release](http://www.scala-lang.org/node/8102) 
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

