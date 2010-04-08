package org.fusesource.scalate.tooling

import _root_.org.junit.Test

/**
 * @version $Revision : 1.1 $
 */
class ArchetypeGuiceTest extends ArchetypeTestSupport {

  @Test
  def testArchetype: Unit = {
    testScalateArchetype("scalate-archetype-guice", true);
  }

}