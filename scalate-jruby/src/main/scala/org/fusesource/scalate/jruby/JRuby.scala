package org.fusesource.scalate.jruby

import java.io.StringWriter
import org.jruby.RubyInstanceConfig
import org.jruby.embed.{ LocalContextScope, ScriptingContainer }

/**
 * A simple interface to the jruby interpreter
 */
class JRuby {

  var container = new ScriptingContainer(LocalContextScope.SINGLETON)
  container.setCompileMode(RubyInstanceConfig.CompileMode.JIT)

  RubyInstanceConfig.FASTEST_COMPILE_ENABLED = true
  RubyInstanceConfig.FASTSEND_COMPILE_ENABLED = true

  def run(scriptlet: String*): Either[(Throwable, String), AnyRef] = this.synchronized {
    val errors: StringWriter = new StringWriter
    try {
      container.setErrorWriter(errors)
      Right(container.runScriptlet(scriptlet.mkString("\n")))
    } catch {
      case e: Throwable =>
        Left((e, errors.toString))
    }
  }

  def put(name: String, value: AnyRef) = container.put(name, value)

}
