package org.fusesource.scalate
package support

import org.fusesource.scalate.util.{Logging, ClassLoaders, Files}

/**
 * A TemplatePackage where we try and find an object/controller/resource type based on the name of the current template and if we can
 * find it then we create a variable called **it** of the controller type and import its values into the template.
 *
 * This approach can be used for JAXRS controllers or for template views of objects. It avoids having to explicitly
 * import the controller or 'it' variable from the attribute scope
 */
class DefaultTemplatePackage extends TemplatePackage with Logging {
  def header(source: TemplateSource, bindings: List[Binding]) = {
    bindings.find(_.name == "it") match {
      case Some(b) =>
        // already has a binding so don't do anything
        ""
      case _ =>
        val cleanUri = source.uri.stripPrefix("/")
        val extensions = cleanUri.split('.').tail
        var className = cleanUri.replace('/', '.')
        extensions.map {
          e =>
            className = Files.dropExtension(className)
            ClassLoaders.findClass(className)
        }.find(_.isDefined).getOrElse(None) match {
          case Some(clazz) =>
            // note we can use the simple name as our template should be in the same package
            val it = "val " + variableName + " = attribute[" + clazz.getSimpleName + "](\"" + variableName + "\")\n"
            if (importMethod) {
              it + "import it._\n"
            } else {
              it
            }

          case _ =>
            debug("Could not find a class on the classpath based on the current url: " + cleanUri)
            ""
        }
    }
  }

  /**
   * The name of the variable
   */
  def variableName = "it"

  /**
   * Returns whether or not the methods on the variable should be imported
   */
  def importMethod = true
}