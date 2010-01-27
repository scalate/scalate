package org.fusesource.scalate

/**
 * Created by IntelliJ IDEA.
 * User: chirino
 * Date: Jan 27, 2010
 * Time: 1:47:33 PM
 * To change this template use File | Settings | File Templates.
 */

case class TemplateArg(name:String, clazz:Class[_]=classOf[Any], importMembers:Boolean=false, defaultValue:String) {
}