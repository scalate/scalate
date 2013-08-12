# Scalate User Guide

* Table of contents
{:toc}

Scalate is a template engine based on the Scala language.

## Features

* Supports multiple template syntaxes
  * [SSP](ssp-reference.html) which is like [Velocity](http://velocity.apache.org/), JSP or Erb from Rails 
  * [Scaml](scaml-reference.html) which is a Scala dialect of [Haml](http://haml-lang.com/) along with the [Jade syntax](jade.html)
  * [Mustache](mustache.html) which is a Scala dialect of [Mustache](http://mustache.github.com/) for logic-less templates which also work inside the browser using [mustache.js](http://github.com/janl/mustache.js)

* Support for [layouts](#layouts) of templates and wiki markup
* Has a powerful [console](console.html) and [command line shell](tool.html) with Scalate converters for [JSP](jspConvert.html) or [HTML](htmlConvert.html)
* Works well with a number of [frameworks](frameworks.html) or easily [embed into your application](scalate-embedding-guide.html)
* Can be used in any web application or used in a standalone application to generate things like emails or source code or even used to generate your [static or semi-static website](siteGen.html).

## Template Languages

Scalate supports a number of different template languages as template languages have various different sweet spots depending on your requirements.

### Ssp (Scala Server Pages)

If you know [Velocity](http://velocity.apache.org/), JSP or Erb from Rails then hopefully the syntax of Ssp is familiar; only using Scala as the language of expressions and method invocations.

{pygmentize_and_compare::}
-----------------------------
ssp: .ssp file
-----------------------------
<%@ var user: User %>
<p>Hi ${user.name},</p>
#for (i <- 1 to 3)
<p>${i}</p>
#end
<p>See, I can count!</p>
-----------------------------
xml: produces
-----------------------------
<p>Hi James,</p>
<p>1</p>
<p>2</p>
<p>3</p>
<p>See, I can count!</p>
{pygmentize_and_compare}

For full documentation of the Ssp syntax see the [Ssp Reference Guide](ssp-reference.html)


### Scaml (Scala Markup Language)

Scaml is a markup language
that's used to cleanly and simply describe the XHTML of any web document,
without the use of inline code.  It is Scala version of
[Haml](http://haml-lang.com/).
Scaml functions as a replacement
for inline page templating systems such as PHP, ERB, and ASP.
However, Scaml avoids the need for explicitly coding XHTML into the template,
because it is actually an abstract description of the XHTML,
with some code to generate dynamic content.

{pygmentize_and_compare::}
-----------------------------
scaml: .scaml file
-----------------------------
-@ var user: User
%p Hi #{user.name},
- for(i <- 1 to 3)
  %p= i
%p See, I can count!
-----------------------------
xml: produces
-----------------------------
<p>Hi James,</p>
<p>1</p>
<p>2</p>
<p>3</p>
<p>See, I can count!</p>
{pygmentize_and_compare}

For full documentation of the Scaml syntax see the [Scaml Reference Guide](scaml-reference.html)

### Jade 

The [Jade syntax](jade.html) is similar to [Scaml](scaml-reference.html), its a [modified dialect](http://jade-lang.com/) of [Haml](http://haml-lang.com/) where element names do not require a leading % symbol which can make it a little easier to read.

{pygmentize_and_compare::}
-----------------------------
jade: .jade file
-----------------------------
-@ var user: User
p Hi #{user.name},
- for(i <- 1 to 3)
  p= i
p See, I can count!
-----------------------------
xml: produces
-----------------------------
<p>Hi James,</p>
<p>1</p>
<p>2</p>
<p>3</p>
<p>See, I can count!</p>
{pygmentize_and_compare}

For more details see the [Jade reference](jade.html) 
 
### Mustache

The [Scalate Mustache](mustache.html) template language is a Scala dialect of cross-language [Mustache](http://mustache.github.com/) template engine for logic-less templates which also work inside the browser using [mustache.js](http://github.com/janl/mustache.js). 

Mustache is logic-less, using simple tags which can be used to represent loops, expressions or logical branching.

Given the following attributes:

{pygmentize:: scala}
Map(
  "name" -> "Chris",
  "value" -> 10000,
  "taxed_value" -> 10000 - (10000 * 0.4),
  "in_ca" -> true
  )
{pygmentize}

Then the following mustache file will generate

{pygmentize_and_compare::}
-----------------------------
text: .mustache file
-----------------------------
Hello {{name}}
You have just won ${{value}}!
{{#in_ca}}
Well, ${{taxed_value}}, after taxes.
{{/in_ca}}
-----------------------------
text: produces
-----------------------------
Hello Chris
You have just won $10000!
Well, $6000.0, after taxes.
{pygmentize_and_compare}

For more detail see the [Mustache Reference](mustache.html)

## Calling Scala functions

Its very simple to invoke any scala function inside Scalate. By default if the function you call returns NodeSeq then the output will already be properly XML encoded; so things output nicely without any possible cross scripting hacks etc.

For example the following function creates a hypertext link using Scala's XML support

{pygmentize:: scala}
object Cheese {
  def foo(productId: Int) = 
    <a href={"/products/" + productId} title="Product link">My Product</a>
}
{pygmentize}

This function can be invoked in your [Ssp](ssp-reference.html) code as

{pygmentize:: ssp}
<% import Cheese._  %>
${foo(123)}
{pygmentize}

If your template is in the same package as the <b>Cheese</b> class then the import is not required.

The [Scaml](scaml-reference.html) version is

{pygmentize:: scaml}
- import Cheese._
= foo(123)    
{pygmentize}


### Accessing request state inside a Scala function {#accessing_request_state_inside_a_scala_function}

As you write snippet functions for use in your templates you might find yourself needing to access the current HttpServletRequest or HttpServletResponse.

There is a simple helper import you can use...

{pygmentize:: scala}
import org.fusesource.scalate.servlet.ServletRenderContext._

object MySnippets {
  def foo = {
    // thanks to the import I now have access to the renderContext
    // along with the standard servlet objects:
    // request, response, servletContext, servletConfig
    request.getParameter("foo") 
  }
}
{pygmentize}

This helps you keep your snippet functions nice and small.


### Passing a template block to a Scala function {#passing_a_template_block_to_a_scala_function}

To use the JSP concept of custom tags, you might want to pass a block of template to a function for further processing or transformation.

This can be done by just adding a parameter list of the form <b>(body: => Unit)</b> to your method. For example

{pygmentize:: scala}
import org.fusesource.scalate.RenderContext.capture

object Cheese {
  def foo(productId: Int)(body: => Unit) = 
    <a href={"/products/" + productId} title="Product link">capture(body)</a>
}
{pygmentize}

See how the body is captured using the <b>capture(body)</b> function. Now the text of the hypertext link can be specified as a block of template in [Ssp](ssp-reference.html)

{pygmentize:: ssp}
<%@ val id: Int = 123 %>
<% import Cheese._  %>

<%= foo(id) { %>
  product ${id}
<% } %>
{pygmentize}

This should generate something like

{pygmentize:: xml}
<a href="/products/123" title="Product link">product 123</a>
{pygmentize}

Or using [Velocity style directives](ssp-reference.html#velocity_style_directives) this might look like this

{pygmentize:: ssp}
#do(foo(id))
product ${id}
#end
{pygmentize}


The [Scaml](scaml-reference.html) version is

{pygmentize:: scaml}
-@ val id: Int = 123
- import Cheese._

= foo(id)  
  product #{id}  
{pygmentize}

Notice the [Scaml](scaml-reference.html) version is simpler, not requiring the open and close { } tokens as it uses indentation.


## Views

From within your Scala code or inside a template you often want to render an object or collection of objects. 
Scalate uses a convention over configuration mechanism so that you can render any object using a simple method call in your template. e.g. in SSP

{pygmentize:: ssp}
<% val user = new User("foo") %>
<p>Something...</p>

<% view(user) %>

<p>... more stuff </p>
{pygmentize}

### View names

The view method takes a model object and an optional view name. The view name defaults to _"index"_ if you do not specify one. For exmaple you could have various views for an object such as _"index", "edit", "detail", etc._ Then you might want to show the edit view of an object via

{pygmentize:: ssp}
<% val user = new User("foo") %>

${view(user, "edit")}
{pygmentize}


Scalate will then look for the template called _packageDirectory.ClassName.viewName.(jade|mustache|ssp|scaml)_ and render that. For example in the sample web application to render an _org.fusesource.scalate.sample.Person_ object Scalate uses the [org/fusesource/scalate/sample/Person.index.ssp template](https://github.com/scalate/scalate/blob/master/samples/scalate-sample/src/main/webapp/WEB-INF/org/fusesource/scalate/sample/Person.index.ssp).

Notice that since the view is defined within the package of the model object, there is no need to import the _org.fusesource.scalate.sample.Person_, instead you can just refer to the model type directly as _Person_.

If a template is not found for the exact class name then the class and interface (trait) hierarchies are walked until one is found.

So for example you could provide a template for a generic trait you have - such as a template to render any [scala.Product](https://github.com/scalate/scalate/blob/master/samples/scalate-sample/src/main/webapp/WEB-INF/scala/Product.index.ssp) which will then render any case class; then you can customise the view on a class by class basis as required.

### The 'it' variable

By default we use the variable named _it_ to refer to the model parameter. This convention means that when working with JAXRS and Jersey's implicit views the model object is implicitly available to any templates using this naming convention.

### DRY IT

Given how common object views are and templates where you pass in an object to render, the default behaviour of [Scalate Packages](#dry) is to automatically import a value based on the type of the template if you follow the object naming convention shown above.

For example if your template matches the name **package/className.viewName.extension** then the className is implicitly imported into your template as a typesafe attribute.

For example if your template is org/fusesource/scalate/sample/Person.index.ssp then it can look like this

{pygmentize:: ssp}
name: ${firstName} ${surname}
{pygmentize}

Which has an implicit import of the 'it' variable so the above template is equivalent to the more explicit (but less DRY) alternative:

{pygmentize:: ssp}
<%@ import val it: Person %>
name: ${firstName} ${surname}
{pygmentize}

Or the even less DRY

{pygmentize:: ssp}
<%@ val it: Person %>
name: ${it.firstName} ${it.surname}
{pygmentize}

### Collections

If you have a collection of objects you wish to view then you can use a simple helper method called *collection* which works like the *view* method described above.

{pygmentize:: ssp}
<% val people = List(Person("James", "Strachan"), Person("Hiram", "Chirino")) %>
<% collection(people) %>
{pygmentize}

As with the *view* method you can specify an optional view name if you won't want to use the _"index"_ default.

Also you can specify a separator to use between views of objects. The following example shows a horizontal line between views...

{pygmentize:: ssp}
<% val people = List(Person("James", "Strachan"), Person("Hiram", "Chirino")) %>
<% collection(people, separator = "<hr/>")  %>
{pygmentize}

Or without using named arguments

{pygmentize:: ssp}
<% collection(people, "index", "<hr/>")  %>
{pygmentize}

If a collection contains different types of objects then the correct view will be used for each element in the collection.

You can also supply a function for the separator if you want it to be dynamic

{pygmentize:: ssp}
<% var x = 1 %>
collection(people, separator = {x += 1; <h3>Person {x}</h3>})
{pygmentize}


## Render templates

It is common to want to refactor large templates into smaller reusable pieces. Its easy to render a template from inside another template with the *render* method as follows

{pygmentize:: ssp}
<% render("foo.ssp") %>
{pygmentize}

This will render a template called *foo.ssp* relative to the current template. You can use absolute names if you prefer

{pygmentize:: ssp}
<% render("/customers/contact.ssp") %>
{pygmentize}

You can also pass parameters into the template if it takes any

{pygmentize:: ssp}
<% render("/customers/contact.ssp", Map("customer" -> c, "title" -> "Customer")) %>
{pygmentize}

When passing attributes you can use the Scala symbol notation for keys if you prefer...

{pygmentize:: ssp}
<% render("/customers/contact.ssp", Map('customer -> c, 'title -> "Customer")) %>
{pygmentize}

If you prefer you can pass in a body to the template using the *layout* method as described in [using explicit layouts inside a template](#explicit_layouts_inside_a_template).


## Layouts {#layouts}

Its quite common to want to style all pages in a similar way; such as adding a header and footer, a common navigation bar or including a common set of CSS stylesheets.

You can achieve this using the layout support in Scalate.

All you need to do is create a layout template in _/WEB-INF/scalate/layouts/default.ssp_ (or _/WEB-INF/scalate/layouts/default.scaml_ if you prefer). Here is a simple example layout which lays out the body and lets the title be customized on a per page basis.

{pygmentize:: ssp}
<%@ var body: String %>
<%@ var title: String = "Some Default Title" %>
<html>
<head>
  <title>${title}</title>
</head>
<body>
  <p>layout header goes here...</p>

  ${unescape(body)}

  <p>layout footer goes here...</p>
</body>
</html>
{pygmentize}

Then all pages will be wrapped in this layout by default.

This means your templates don't need to include the whole html/head/body stuff, typically you'll just want the actual content to be displayed in the part of the layout you need. So a typical page might look like this...

{pygmentize:: ssp}
<h3>My Page</h3>
<p>This is some text</p>
{pygmentize}


### Changing the title or layout template

To set parameters on a layout or to change the layout template used, just output attribute values in your template.

{pygmentize:: ssp}
<% attributes("layout") = "/WEB-INF/layouts/custom.ssp" %>
<% attributes("title") = "This is the custom title" %>
<h3>Custom page</h3>
<p>This is some text</p>
{pygmentize}


### Disabling layouts

If you wish to disable the use of the layout on a template, just set the layout attribute to "" the empty string.

{pygmentize:: ssp}
<% attributes("layout") = "" %>
<html>
<body>
  <h1>No Layout</h1>
  <p>This page does not use the layout</p>
</body>
</html>
{pygmentize}

To see examples of layouts in use, try running the sample web application and looking at the layout related example pages.

### Explicit layouts inside a template {#explicit_layouts_inside_a_template}

You may want to layout some content within part of your template explicitly rather than just applying a layout to an entire page.

For example you may want to create a layout as follows in file _foo.ssp_

{pygmentize:: ssp}
<%@ val body: String = "Bar" %>
<table>
 <tr>
   <th>Some Header</th>
 </tr>
 <tr>
   <td><%= body %></td>
 </tr>
</table>
{pygmentize}

Then we can invoke this template passing in the body as follows in [Ssp](ssp-reference.html)

{pygmentize:: ssp}
<% render("foo.ssp", Map("body" -> "Foo")) %>
{pygmentize}

However if you want to pass in the body as a block of template you can use the *layout* method as follows

{pygmentize:: ssp}
<% layout("foo.ssp") {%>
Foo
<%}%>
{pygmentize}

Or using [Velocity style directives](ssp-reference.html#velocity_style_directives) this might look like this

{pygmentize:: ssp}
#do( layout("foo.ssp") )
Foo
#end
{pygmentize}



Both will generate the same response.

Using the above mechanism via either the *render* or *layout* methods is quite like creating a JSP custom tag inside a .tag file if you come from a JSP background. 

The nice thing is there's really no difference technically between a regular template, a layout or a 'tag' template or a 'partial' (to use Rails terminology), they are all just templates which can have parameters which can be mandatory or optional.


## Capturing output

Sometimes you may wish to capture the result of rendering a block of template, assign it to a variable and then pass it as an argument to some method. For this the *capture* method can be used.

For example

{pygmentize:: ssp}
<% val foo = capture { %>
  hello there ${user.name} how are you?
<%}%>
...
${foo}
...
${foo}
{pygmentize}

We capture the block which generates a greeting, assign it to the _foo_ variable which we can then render or pass into methods etc.

The [Scaml](scaml-reference.html) version of this is a bit more concise

{pygmentize:: scaml}
- var foo = capture
  hello there #{user.name} how are you?
    
...
= foo
...
= foo
{pygmentize}


## Making templates more DRY {#dry}

When you create a number of templates in a directory you might find you are repeating the same sets of imports across many templates. This doesn't feel terribly DRY. Scala 2.8 supports [package objects](http://programming-scala.labs.oreilly.com/ch07.html#PackageObjects) which allows you to define types, variables and methods at the package scope to be reused inside classes and traits defined inside the package.

So Scalate supports a similar feature for templates which are code generated like [SSP](ssp-reference.html), [Scaml](scaml-reference.html) and [Jade](jade.html).

The basic idea is Scalate will look in the same package as the template for a Scala/Java class called **ScalatePackage** which must extend [TemplatePackage](http://scalate.fusesource.org/maven/${project_version}/scalate-core/scaladocs/org/fusesource/scalate/support/TemplatePackage.html). If there is no ScalatePackage in the package, its parent package is searched all the way to the root package (i.e. no package name). 

If a ScalatePackage class is found then its **header** method is invoked to generate any shared imports, variables or methods across templates.

For example you could add the following Scala code to a file called **src/main/scala/foo/ScalatePackage.scala** to add some default imports you want to share across a number of templates in a the **foo** directory and its descendants...

{pygmentize:: scala}
package foo

import org.fusesource.scalate.TemplateSource
import org.fusesource.scalate.support.TemplatePackage


/**
 * Defines some common imports, attributes and methods across templates in package foo and below
 */
class ScalatePackage extends TemplatePackage {

  /** Returns the Scala code to add to the top of the generated template method */
   def header(source: TemplateSource, bindings: List[Binding]) = """

// some shared imports
import com.acme._
import com.acme.something.MyHelper._

// some helper methods
// would be better being imported from a helper class like MyHelper above
def time = new java.util.Date()

  """
}

{pygmentize}

You can then use the usual expressive composition features of Scala to use inheritance, traits, delegation and so forth to decide how to spread this code across your templates and decide how to combine these things at the package level to be inherited by all child packages and templates. You might find moving templates into functional directories makes it easier to reuse common boilerplate imports, values and methods across templates.


## Scalate Samples

### Getting Started

The easiest way to get started is to try the [Getting Started Guide](getting-started.html)

### Running the Sample Web Application

The [source code](../source.html) comes with a sample web application called **scalate-sample** which includes a number of exmample templates you can play around with>

Scalate can be built either using Maven or SBT

#### Requirements

General Requirements:

* Java 5 runtime or Newer
* Scala 2.8 

Web Container Requirements:

* Servlet 2.5 container or Newer

#### Using Maven

Install [Maven](http://maven.apache.org/) version 2.0.9 or later. Then type

{pygmentize:: text}
mvn install
{pygmentize}

To run the sample web application

{pygmentize:: text}
cd scalate-sample
mvn jetty:run
{pygmentize}

Then open [the sample home page](http://localhost:8080/)

#### Using SBT

You can also use [sbt](http://code.google.com/p/simple-build-tool/ "simple build tool") to build Scalate.

To setup your sbt environment and import the dependencies from the maven pom files type

{pygmentize:: text}
./sbt
update
{pygmentize}

Then to build the code type

{pygmentize:: text}
compile
{pygmentize}

to run the tests 

{pygmentize:: text}
test
{pygmentize}

For more information see the [sbt building instructions](../sbt.html)


## Using Scalate

You might want to refer to the [Frameworks Documentation](frameworks.html) to see if there is some specific instructions on using Scalate with your favourite web framework.

### Using Scalate as Servlet filter in your Web Application {#using_scalate_as_servlet_filter_in_your_web_application}

* Add something like the following to your web.xml file to support Scalate templates:

{pygmentize:: xml}
  <filter>
    <filter-name>TemplateEngineFilter</filter-name>
    <filter-class>org.fusesource.scalate.servlet.TemplateEngineFilter</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>TemplateEngineFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
{pygmentize}

*  Include the following JARs in your servlet's runtime environment (probably in WEB-INF/lib):
    * scala-compiler.jar
    * scala-library.jar
    * scalate-core.jar

You could add one or more of the above to your servlet container's server-wide configuration if you prefer.

#### Mapping URIs to templates with the Filter

The Scalate template filter looks for templates in several locations to satisfy a request.  
For example, if requested URI is `/path/file.html`, then it will for templates in this order:

1. `/path/file.html.${ext}`
2. `/WEB-INF/path/file.html.${ext}`
3. `/path/file.${ext}`
4. `/WEB-INF/path/file.${ext}`

Where `${ext}` gets replaced with all the template extensions supported by the Template Engine.  If the requested
URI already ends with template extension then it would get looked up in the root and under the `/WEB-INF` directory.

### Using Scalate with JAXRS/Jersey

Our recommendation is to start with [JOG](jog.html) (Jersey on Guice).

To get up to speed quickly with JOG try the [Getting Started Guide](getting-started.html) which uses the [Scalate Tool](tool.html) and [WAR Overlay](war-overlay.html) to include the [Console](console.html) in your web application.

### Embedding Scalate in your Application or Framework

Scalate does not have any hard dependencies on a web framework or even HTTP.  It can be used as a standalone
rendering engine in your application.  For more information on how to embed in your application, please reference the 
[Scalate Embedding Guide](scalate-embedding-guide.html)

### Working Directory, Caching, Reloading

Scalate uses a *working directory* to store the generated scala source files and the compiled JVM bytecode for templates. This can be configured on a [TemplateEngine](http://scalate.fusesource.org/maven/${project_version}/scalate-core/scaladocs/org/fusesource/scalate/TemplateEngine.html) using the **workingDirectory** property. If no configuration is made Scalate will use the *scalate.workdir* system property by default.

The [archetypes](archetypes.html) or projects created by the [scalate tool](tool.html) or the modules in the [scalate source](../source.html) all set the **scalate.workdir** to be the maven property of the same name; which defaults to *target/\_scalate*

If you wanted to run a web application using a different directory, such as _/tmp_ you could do

{pygmentize:: text}
mvn -Dscalate.workdir=/tmp jetty:run
{pygmentize}

In production settings you can disable the caching and reloading of templates if you wish using the **allowCaching** and **allowReload** properties on [TemplateEngine](http://scalate.fusesource.org/maven/${project_version}/scalate-core/scaladocs/org/fusesource/scalate/TemplateEngine.html) which default to **scalate.allowCaching** and **scalate.allowReload** respectively.

### Configuring the TemplateEngine in your web app

Scalate supports a standard bootstrap mechanism which tries to be framework agnostic so it can work in servlets or using the servlet filter or works when generating [static websites](siteGen.html) and is easy to plugin to other frameworks.

Just add a class called **scalate.Boot** which should be a class with a method called **run()** which can then do whatever you need to configure things before the template engine starts to render your templates.

If you need access to the TemplateEngine then just add a constructor argument. (You can also pass in ServletContext as a constructor parameter if you are inside a web application and other values which may come from your framework).

e.g.

{pygmentize:: scala}
package scalate

import org.fusesource.scalate.TemplateEngine
import java.io.File

class Boot(engine: TemplateEngine) extends Logging {

  def run: Unit = {
    // lets change the workingDirectory
    engine.workingDirectory = new File("myScalateWorkDir")
  }
}
{pygmentize}



### Precompiling Templates {#precompiling_templates}

Scalate currently lazily compiles templates on the fly, then it will cache
the compiled template and only recompile it if it detects the source template
has changed.

In production you probably want all your templates to be precompiled so that

* you can detect at build time any typos in your templates, particularly if
  you are using [Scaml](scaml-reference.html) or [Ssp](ssp-reference.html)
  which use static type checking for all expressions in your template.

* all your templates are immediately available as a fast, compiled JVM .class
  file rather than taking the overhead of the first request causing a compile
  phase

To do this you just need to include the *maven-scalate-plugin* into your project. The plugin only precompiles when you are packaging into a war to avoid slowing down your development mode cycles.

The [archetypes](archetypes.html) created by the [scalate tool](tool.html) come with this plugin enabled already.

Otherwise you can just add this to your pom.xml

{pygmentize:: xml}
<build>
  <plugins>
    <plugin>
      <groupId>org.fusesource.scalate</groupId>
      <artifactId>maven-scalate-plugin_#{scala_compat_tag}</artifactId>
      <version>${project_version}</version>
      <executions>
        <execution>
          <goals>
            <goal>precompile</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
{pygmentize}

#### Precompiling templates with SBT

The [archetypes](archetypes.html) created by the [scalate tool](tool.html)
have their SBT build configuration setup so that the templates are
precompiled before they are packaged.  If you need to add the precompiler
to an existing sbt project then you need to first add the plugin
dependency:

    lazy val scalate_plugin = "org.fusesource.scalate" % "sbt-scalate-plugin_${scala_compat_tag}" % "${project_version}"

And then in your WebProject, you will need to add the
`org.fusesource.scalate.sbt.PrecompilerWebProject` trait.  And then make sure
the Scalate dependencies are added to the project.  For example:

    class Project(info: ProjectInfo) extends 
          DefaultWebProject(info) with 
          PrecompilerWebProject {
      
      lazy val scalate_core = "org.fusesource.scalate" % "scalate-core_${scala_compat_tag}" % "${project_version}" 
      lazy val servlet = "javax.servlet" % "servlet-api"% "2.5" 
      lazy val logback = "ch.qos.logback" % "logback-classic" % "0.9.26"
      
    }

### Using Scalate on GAE 

If you are using Scalate on [Google AppEngine](http://code.google.com/appengine/) (GAE) then you will probably want to precompile all your templates before you deploy them; so that each request is processed very quickly - so app engine won't kill your thread midway through.

To see an example of a Scalate project already setup using GAE try the [the hello-scalate-appengine project](http://github.com/Yasushi/hello-scalate-appengine) by Yasushi Abe.

### Possible Gotchas

#### Class Loaders

Scalate can sometimes struggle with ClassLoaders. This is due to the Scala compiler requiring an explicit class path to be specified as a String of names rather than taking an actual ClassLoader object. 

So Scalate works fine with expanded WARs, or servlet containers who's ClassLoader implements URLClassLoader or an AntClassLoader like thing or the Play Framework. But you might be able to find some application server that doens't provide an easy-to-diagnose ClassLoader object which may require an explicitly configured class path for compiling. 

A work around is to precompile your templates with the maven plugin (see the section above).

#### Character Encoding

Scalate currently assumes that

- template source files are all UTF-8-encoded.

- template output is all UTF-8-encoded.


## IDE plugins

Using an IDE plugin can make it much easier to view and edit Scalate templates.

### TextMate plugin

If you use [TextMate](http://macromates.com/) (which on OS X is a great text editor) you can install the [Scalate plugin](http://github.com/scalate/Scalate.tmbundle) as follows:

{pygmentize:: text}
cd ~/Library/Application\ Support/TextMate/Bundles/
git clone git://github.com/scalate/Scalate.tmbundle.git
{pygmentize}

If you have not already done so you will also need a Scala plugin for TextMate which the [Ssp](ssp-reference.html) and [Scaml](scaml-reference.html) languages uses for the Scala code blocks.

We like the [version by Dean Wampler](http://github.com/deanwampler/Scala.tmbundle) though there's a few around github and one included in sbaz in the Scala distro too.

{pygmentize:: text}
cd ~/Library/Application\ Support/TextMate/Bundles/
git clone git://github.com/deanwampler/Scala.tmbundle.git
{pygmentize}

When you restart TextMate you should now get syntax highlighting and more when you open up either a [Ssp](ssp-reference.html) or  [Scaml](scaml-reference.html) file. 

The current plugin does not highlight Scala expressions terribly well with the default Mac Classic colour scheme in TextMate. We found that it helps to add an extra colour to your scheme.

* open TextMate preferences via the TextMate menu or by hitting the apple key and ','
* select Fonts & Colors
* hit + to add a new entry
* type "Scaml expression" and pick a colour to use such as dark green (to differentiate from static markup text)
* in the **Scope Selector:** field enter **source.scala**
* you now should see Scala expressions in your Scaml file highlighted in green (so you can more easily tell the difference between static text and scala expressions)

### If you are an IDE hacker

We created Scalate specifically to be IDE friendly and we'd love to help create more and better IDE plugins (plus we [love contributions](../contributing.html)!). It should be easy to reuse any JSP / Erb / HAML IDE plugins but just swizzle them a little to use the Scala language instead - then get all the benefits of smart completion from Scala's static type system.

We've a [page on writing IDE plugins for Scalate](../creating-ide.html) which has more details on how an IDE plugin should ideally work for Scalate.
