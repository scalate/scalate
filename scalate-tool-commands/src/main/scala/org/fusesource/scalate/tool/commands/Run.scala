/*
 * Copyright (C) 2009, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.fusesource.scalate.tool.commands

import java.io.{File, StringWriter, PrintWriter}
import org.fusesource.scalate.{TemplateEngine, Binding, DefaultRenderContext}
import org.fusesource.scalate.support.FileResourceLoader
import org.fusesource.scalate.tool.Command
import org.fusesource.scalate.tool.Scalate._

/**
 * <p>
 * Implements the 'scalate run' sub command.
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class Run extends Command {

  def name = "run"
  def summary = "Renders a Scalate template file"

  def usage() = {
    intro()
    info("Usage: scalate run [options] file")
    info()
    info("Options:")
    info("  --workdir dir  : Sets the work directory where scalate generates class files to.")
    info("                   defaults to a temporary directory.")
    info("  --root dir     : Sets the root of the tempalte search path. ")
    info("                   defaults to the current directory.")
    info("  --help         : Shows this help screen")
    info()
    info("For more help see http://scalate.fusesource.org/documentation/tool.html")
    info()

  }

  def process(args: List[String]): Int = (new Processor).process(args)

  class Processor {

    var root = new File(".")
    var workdir:File = null

    def process(args: List[String]): Int = {

      args match {
        case "--help" :: the_rest =>
          usage()
          return 0
        case "--workdir" :: dir :: the_rest =>
          this.workdir = new File(dir);
          return process(the_rest)
        case "--root" :: dir :: the_rest =>
          this.root = new File(dir);
          return process(the_rest)
        case template :: rest =>
          return render(template, rest)
        case _ =>
          info("Invalid syntax: template not specified")
          usage()
          return -1
      }
    }

    def render(path:String, args: List[String]): Int = {
  //    val buffer = new StringWriter()
  //    val out = new PrintWriter(buffer)
      try {
        val engine = new TemplateEngine
        if (workdir != null) {
          engine.workingDirectory = workdir
        }
        engine.resourceLoader = new FileResourceLoader(Some(root))

        val writer = new PrintWriter(System.out)
        val context = new DefaultRenderContext(engine, writer);
        context.attributes("args") = args
        context.layout(path)
        writer.flush
        return 0;

      } catch {
        case e:Exception =>
          error("Could not render: "+path, e)
          error("Due to: "+e, e)
          return -1
      }

    }
  }
}
