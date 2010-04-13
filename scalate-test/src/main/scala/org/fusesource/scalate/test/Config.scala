package org.fusesource.scalate.test

/**
 * General test configuration helpers
 * 
 * @version $Revision: 1.1 $
 */
object Config {
  private var _baseDir: String = _

  def baseDir: String = {
    if (_baseDir == null) {
      _baseDir = System.getProperty("basedir", ".")
    }
    _baseDir
  }

  def baseDir_=(value: String): Unit = _baseDir = value
}