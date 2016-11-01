package org.fusesource.scalate
package jrebel

import org.zeroturnaround.javarebel.ClassEventListener
import org.zeroturnaround.javarebel.ReloaderFactory
import util.Logging

/**
 * A JRebel plugin to reload templates in a Scalate TemplateEngine if dependent classes are modified
 */
object ScalateReloader extends TemplateEngineAddOn with Logging {

  /**
   * Reloads templates in a template engine if the classes are modified
   */
  def apply(engine: TemplateEngine): Unit = {
    info("Installing JRebel Scalate plugin on instance: " + engine + " " + System.identityHashCode(engine))

    ReloaderFactory.getInstance.addClassReloadListener(new ClassEventListener {

      def priority = ClassEventListener.PRIORITY_DEFAULT

      def onClassEvent(i: Int, clazz: Class[_]) = {
        // TODO we could be a bit more clever to figure out which templates need to be reloaded
        // for now lets just flush all templates!
        info("Flushing Scalate templates on instance: " + engine)
        engine.invalidateCachedTemplates()
      }
    })
  }
}