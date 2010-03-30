package org.fusesource.scalate.guice

import _root_.com.google.inject.Key
import _root_.com.google.inject.servlet.ServletModule
import _root_.javax.servlet.http.HttpServlet

/**
 * This helper class provides a workaround for using 'with' in the Guice Servlet DSL
 *
 * @version $Revision : 1.1 $
 */

class RichBuilder(builder: ServletModule.ServletKeyBindingBuilder) {
  def by[T <: HttpServlet](servletKey: Class[T]) {builder.`with`(servletKey)};

  def by[T <: HttpServlet](
          servletKey: Class[T], params: java.util.Map[String, String]) {builder.`with`(servletKey, params)};


  def by[T <: HttpServlet](servletKey: Key[T]) {builder.`with`(servletKey)};

  def by[T <: HttpServlet, S](
          servletKey: Key[T],
          params: java.util.Map[String, String]) {builder.`with`(servletKey, params)};


  def withClass[T <: HttpServlet](servletKey: Class[T]) {
    builder.`with`(servletKey)
  };

  def withClass[T <: HttpServlet](
          servletKey: Class[T],
          params: java.util.Map[String, String]) {builder.`with`(servletKey, params)};

  def withClass[T <: HttpServlet](servletKey: Key[T]) {
    builder.`with`(servletKey)
  };

  def withClass[T <: HttpServlet](
          servletKey: Key[T],
          params: java.util.Map[String, String]) {builder.`with`(servletKey, params)};

}

