/**
 * Copyright (C) 2009, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.scalate.util

/**
 * Helpers that aid with Scala soruce code generation.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object SourceCodeHelper {

  /**
   * Gives you the name of the given class as string that
   * can be used in Scala source code.
   */
  def name(clazz:Class[_]):String = {
    split_name(clazz).mkString(".")
  }

  def split_name(clazz:Class[_]):List[String] = {
    if( clazz.getEnclosingClass != null ) {
      split_name(clazz.getEnclosingClass) ::: clazz.getSimpleName :: Nil
    } else if( clazz.getPackage != null ) {
      clazz.getPackage.getName :: clazz.getSimpleName :: Nil
    } else {
      clazz.getName :: Nil
    }
  }
}