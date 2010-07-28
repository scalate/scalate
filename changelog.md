![Scalate][logo]
===============================

[Scalate 1.2](http://scalate.fusesource.org/blog/releases/2010/07/release-1-2.html), released 2010-07-28
----

* TODO!


[Scalate 1.1](http://scalate.fusesource.org/blog/releases/2010/04/release-1-1.html), released 2010-04-15
----

* [Ssp](http://scalate.fusesource.org/documentation/ssp-reference.html#syntax) now supports [Velocity style directives](http://scalate.fusesource.org/documentation/ssp-reference.html#velocity_style_directives) for more concise looping and branching.
* new [Scalate Tool](http://scalate.fusesource.org/documentation/tool.html) for creating new projects with Scalate more easily
* improved API for working with templates from different sources (file, URL, Source, String etc) via the helper methods on [TemplateSource object](http://scalate.fusesource.org/maven/{project_snapshot_version:}/scalate-core/scaladocs/org/fusesource/scalate/TemplateSource$.html) and methods on [TemplateEngine](http://scalate.fusesource.org/maven/{project_snapshot_version:}/scalate-core/scaladocs/org/fusesource/scalate/TemplateEngine.html) which take a [TemplateSource](http://scalate.fusesource.org/maven/{project_snapshot_version:}/scalate-core/scaladocs/org/fusesource/scalate/TemplateSource.html)
* easier to configure whitespace handling via the **escapeMarkup** property on [TemplateEngine](http://scalate.fusesource.org/maven/{project_snapshot_version:}/scalate-core/scaladocs/org/fusesource/scalate/TemplateEngine.html) and [RenderContext](http://scalate.fusesource.org/maven/{project_snapshot_version:}/scalate-core/scaladocs/org/fusesource/scalate/RenderContext.html) so its easy to configure markup escaping for an entire project or enable/disable it within templates.


[Scalate 1.0](http://scalate.fusesource.org/blog/releases/2010/04/release-1-0.html), released 2010-04-06
----

Initial release with support for the following template languages

* [Ssp](http://scalate.fusesource.org/documentation/ssp-reference.html#syntax) which is like a Scala version of JSP or Erb from Rails 
* [Scaml](http://scalate.fusesource.org/documentation/scaml-reference.html) which is a Scala dialect of [Haml](http://haml-lang.com/) for very DRY markup

[logo]: http://scalate.fusesource.org/images/project-logo.png "Scalate"