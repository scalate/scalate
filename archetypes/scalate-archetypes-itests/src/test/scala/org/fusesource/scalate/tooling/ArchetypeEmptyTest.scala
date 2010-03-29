package org.fusesource.scalate.tooling

import org.junit.Test

/**
 * @version $Revision : 1.1 $
 */
class ArchetypeEmptyTest extends ArchetypeTestSupport {

  @Test
  def testArchetype: Unit = {
    testScalateArchetype("scalate-archetype-empty");
  }
}