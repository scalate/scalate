# Scalate Embedding Guide

* Table of contents
{:toc}

Scalate was designed to be easily embedded into your application or framework.

## Creating the Template Engine

The `TemplateEngine` class is your main way that you will be interfacing with Scalate.  It is located
in the `org.fusesource.scalate` package, you you might want to start off with that package import:

    import org.fusesource.scalate._

Once you create and configure and instance of `TemplateEngine`, it can be treated as a thread safe 
singleton.  It has sensible configuration defaults so you could just use the following to compile
and load a template:

    val engine = new TemplateEngine
    val template = engine.load("/path/to/template.ssp")

The `load` method returns `Template` object.  `Template` object is a dynamically byte code compiled
version of the the requested template.  The template engine caches the template for
you and watches the original source files for updates so it pick up changes.  Therefore, it's
recommended that you `load` the template each time you before you use it.

The next step is to render the template.  To do this you first need to create a `RenderContext`.
The `RenderContext` is used to supply the template with data collect the render results.  `RenderContext`
is just an interface in case you want to use a custom implementation, but the supplied 
`DefaultRenderContext` implementation should be suitable for most needs.

The following shows how to render loaded template:

    val buffer = new StringWriter()
    val context = new DefaultRenderContext(engine, new PrintWriter(buffer))
    template.render(context)
    println(buffer.toString)


## Passing Data to the Template

Variables can passed as attributes to the template via the render context.  For example:

    val context = new DefaultRenderContext(engine, new PrintWriter(buffer))
    context.attributes += "name" -> ("Hiram", "Chirino")
    context.attributes += "city" -> "Tampa"
    template.render(context)

A template can then access those attributes once they declare a variable binding.  For example:

    <%@ var name:(String,String) >
    <%@ var city:String >
    <p> Hello ${name._1} ${name._2}, from ${city}. </p>
    
Would be rendered as:

    <p> Hello Hiram Chirino from Tampa. </p>

Each template syntax has it's own way of declaring variable bindings. For more details on
on how to declare variable bindings in your template please reference:

* [Ssp Reference: Binding Variables](ssp-reference.html#binding_variables_) 
* [Scaml Reference: Binding Variables](scaml-reference.html#binding_variables_)

## Implicitly Bound Variables

Implicitly bound variables allow templates to access variables which they have not 
declared via a variable binding statement.  For this to happen, the variable declaration
is specified outside the template, when the template is compiled.

You can configure implicit variable bindings on the `TemplateEngine`. Every template compiled 
will then have variable declared.  For example:

    val engine = new TemplateEngine
    engine.bindings = List(Binding("name", "(String,String)"))
    
You can also specify additional binding variables when you load the template:

    val template = engine.load("/path/to/template.ssp", List(Binding("city", "String")))

In the above example, the `name` and `city` variables would be implicitly bound in the template.
This means that previous example template would now need to omit the explicit variable
declarations of `name` and `city`.  The new template would look like:

    <p> Hello ${name._1} ${name._2}, from ${city}. </p>


### Implicitly Imported Bound Variables

When you configure an implicitly bound variable you choose to have the variable's members imported
into your templates name space.  This is especially handy when you want to automatically expose 
helper methods to your template.

For example, lets say you want all templates to be able to use a method called `log`, then you could


1.  Define the Helper Class

        class MyHelper {
      
          def log(message:String) = {
            ...
          }
      
        }
    
2.  Include it in the the implicit bindings.  Setting the 3rd Binding parameter to true enables 
    the member importing.

        engine.bindings = List(Binding("helper", "MyHelper", true))

3.  Add an instance of the helper to the render context.

        context.attributes += "helper" -> new MyHelper

4.  The method is now available for use in all templates:

        <p> Going to log something..</p>
        <% log("Our template just executed.") %>
    

The out of the box servlet integration that Scalate provides uses the above trick to give all rendered templates
access to an imported "context" variable which is an instance of the `DefaultRenderContext` which is the same 
object which is collecting the render results.  It provides the template a bunch of handy helper methods to do things
like `capture` to get the result of rendering nested content or `filter` to apply a transformation filter to some content.


## Other Resources

* [Scalate User Guide](scalate-user-guide.html)