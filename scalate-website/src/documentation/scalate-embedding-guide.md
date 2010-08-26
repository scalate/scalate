# Scalate Embedding Guide

* Table of contents
{:toc}

Scalate was designed to be easily embedded into your application or framework.

## Creating the Template Engine

The `TemplateEngine` class is your main way that you will be interfacing with Scalate.  It is located
in the `org.fusesource.scalate` package, you you might want to start off with that package import:
{pygmentize:: scala}
import org.fusesource.scalate._
{pygmentize}

Once you create and configure and instance of `TemplateEngine`, it can be treated as a thread safe 
singleton.  It has sensible configuration defaults so you could just use the following to compile
and load a template:
{pygmentize:: scala}
val engine = new TemplateEngine
val template = engine.load("/path/to/template.ssp")
{pygmentize}

The `load` method returns `Template` object.  `Template` object is a dynamically byte code compiled
version of the the requested template.  The template engine caches the template for
you and watches the original source files for updates so it pick up changes.  Therefore, it's
recommended that you `load` the template each time you before you use it.

The next step is to render the template.  To do this you first need to create a `RenderContext`.
The `RenderContext` is used to supply the template with data collect the render results.  `RenderContext`
is just an interface in case you want to use a custom implementation, but the supplied 
`DefaultRenderContext` implementation should be suitable for most needs.

The following shows how to render loaded template:
{pygmentize:: scala}
val buffer = new StringWriter()
val context = new DefaultRenderContext(engine, new PrintWriter(buffer))
template.render(context)
println(buffer.toString)
{pygmentize}


## Passing Data to the Template

Variables can passed as attributes to the template via the render context.  For example:
{pygmentize:: scala}
val context = new DefaultRenderContext(engine, new PrintWriter(buffer))
context.attributes("name") = ("Hiram", "Chirino")
context.attributes("city") = "Tampa"
template.render(context)
{pygmentize}

A template can then access those attributes once they declare a variable binding.  For example:
{pygmentize:: jsp}
<%@ var name:(String,String) %>
<%@ var city:String %>
<p> Hello ${name._1} ${name._2}, from ${city}. </p>
{pygmentize}
    
Would be rendered as:
{pygmentize:: html}
<p> Hello Hiram Chirino from Tampa. </p>
{pygmentize}

Each template syntax has it's own way of declaring variable bindings. For more details on
on how to declare variable bindings in your template please reference:

* [Ssp Reference: Binding Variables](ssp-reference.html#binding_variables_) 
* [Scaml Reference: Binding Variables](scaml-reference.html#binding_variables_)

## Passing Data from the Template elsewhere

If you wish you can export attributes from the template using the attributes on the context in a similar way as above.

For example inside a SSP page:


{pygmentize:: jsp}
<% attributes("title") = "This is my new title" %>
<p> This is some content. </p>
{pygmentize}

Now if the page is using layouts, the title attribute would be set on the page. i.e. this template outputs the title attribute so that it could be used by the layout.

## Implicitly Bound Variables

Implicitly bound variables allow templates to access variables which they have not 
declared via a variable binding statement.  For this to happen, the variable declaration
is specified outside the template, when the template is compiled.

You can configure implicit variable bindings on the `TemplateEngine`. Every template compiled 
will then have variable declared.  For example:
{pygmentize:: scala}
val engine = new TemplateEngine
engine.bindings = List(Binding("name", "(String,String)"))
{pygmentize}
    
You can also specify additional binding variables when you load the template:
{pygmentize:: scala}
val template = engine.load("/path/to/template.ssp", List(Binding("city", "String")))
{pygmentize}

In the above example, the `name` and `city` variables would be implicitly bound in the template.
This means that previous example template would now need to omit the explicit variable
declarations of `name` and `city`.  The new template would look like:
{pygmentize:: jsp}
<p> Hello ${name._1} ${name._2}, from ${city}. </p>
{pygmentize}


### Implicitly Imported Bound Variables

When you configure an implicitly bound variable you choose to have the variable's members imported
into your templates name space.  This is especially handy when you want to automatically expose 
helper methods to your template.

For example, lets say you want all templates to be able to use a method called `log`, then you could


1.  Define the Helper Class
    {pygmentize:: scala}
    class MyHelper {
  
      def log(message:String) = {
        ...
      }
  
    }
    {pygmentize}
    
2.  Include it in the the implicit bindings.  Setting the 3rd Binding parameter to true enables 
    the member importing.
    {pygmentize:: scala}
    engine.bindings = List(Binding("helper", "MyHelper", true))
    {pygmentize}

3.  Add an instance of the helper to the render context.
    {pygmentize:: scala}
    context.attributes += "helper" -> new MyHelper
    {pygmentize}

4.  The method is now available for use in all templates:
    {pygmentize:: jsp}
    <p> Going to log something..</p>
    <% log("Our template just executed.") %>
    {pygmentize}

The out of the box servlet integration that Scalate provides uses the above trick to give all rendered templates
access to an imported "context" variable which is an instance of the `DefaultRenderContext` which is the same 
object which is collecting the render results.  It provides the template a bunch of handy helper methods to do things
like `capture` to get the result of rendering nested content or `filter` to apply a transformation filter to some content.

## Configuring the `TemplateEngine`

### Working Directory

The template engine needs a working directory to generate scala code and java byte codes associated with 
the templates.  By default, it use a location under your java temporary directory.  You probably want to change
this so that it uses data location configured for your application:
{pygmentize:: scala}
engine.workingDirectory = new File("/var/lib/myapp/work")
{pygmentize}

### Compiler Class Path

Scalate needs to know what class path to compile the templates against.  By default it builds a class path using
all the jars in the `ClassLoader` which loaded the Scalate jar.  If you are in fancy mutli-`ClassLoader` application,
like OSGi, they this simple heuristic will not work and you will need to specify the class path that the `TemplateEngine`
should compile against.  For example:

{pygmentize:: scala}
engine.classpath = "/path/to/lib.jar:/path/to/another-lib.jar"
{pygmentize}


### Custom Template Loading

In the default configuration, templates are loaded from the file system.  The path you pass to the `TemplateEngine.load`
method is expected to file path to an actual template file.  If you want to load templates from different location, perhaps the classpath or database you will need to supply the `TemplateEngine` a custom implementation of `ResourceLoader`.

Here's simple example that loads a dynamically generated template:
{pygmentize:: scala}
engine.resourceLoader = new FileResourceLoader {
  override def resource(uri: String): Option[Resource] =
    Some(Resource.fromText(uri, "Some text"))
}
{pygmentize}

### Template Cache Configuration

If you are running in production it may make sense to disable template reloading.  
It should be slightly quicker as it avoids doing file system checks for template modifications:
{pygmentize:: scala}
engine.allowReload =  false
{pygmentize}

If you have a large number of templates and would rather not cache them in as compiled java classes in
memory, you can disable template caching altogether with:
{pygmentize:: scala}
engine.allowCaching =  false
{pygmentize}


<!--
TODO: Cover adding CodeGenerator and Filter extensions.
-->

## Other Resources

* [User Guide](user-guide.html)
* [Documentation](index.html)

