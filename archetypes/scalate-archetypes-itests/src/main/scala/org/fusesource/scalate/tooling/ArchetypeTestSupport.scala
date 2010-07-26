/**
 * Copyright (C) 2009-2010 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
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

package org.fusesource.scalate.tooling

import _root_.org.fusesource.scalate.util.IOUtil._
import _root_.org.apache.maven.project.{DefaultProjectBuildingRequest, ProjectBuilder, MavenProject}
import _root_.org.junit.{After, Before, Assert}
import _root_.scala.collection.JavaConversions._
import org.codehaus.plexus.{DefaultContainerConfiguration, ContainerConfiguration, DefaultPlexusContainer}
import org.apache.maven.Maven
import org.apache.maven.exception.DefaultExceptionHandler
import org.apache.maven.exception.ExceptionHandler
import org.apache.maven.exception.ExceptionSummary
import org.apache.maven.execution.DefaultMavenExecutionRequest
import org.apache.maven.execution.MavenExecutionRequest
import org.apache.maven.execution.MavenExecutionRequestPopulator
import org.apache.maven.execution.MavenExecutionResult
import org.apache.maven.lifecycle.LifecycleExecutionException
import org.apache.maven.repository.RepositorySystem
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest
import org.apache.maven.settings.building.SettingsBuilder
import org.apache.maven.settings.building.SettingsBuildingRequest
import org.apache.maven.settings.building.SettingsBuildingResult
import org.codehaus.plexus.util.FileUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.util.Collections
import java.util.{List => JList}
import java.util.LinkedHashMap
import java.util.Map
import java.util.Properties
/**
 * @version $Revision : 1.1 $
 */

class ArchetypeTestSupport {
  protected val logger = LoggerFactory.getLogger(getClass)
  protected var baseDir = new File(System.getProperty("basedir", ".")).getAbsoluteFile
  protected var container: DefaultPlexusContainer = null

  // TODO discover this from the pom!
  protected var version: String = _

  protected var newProjectDir: File = _

  protected def testScalateArchetype(artifactId: String, testConsole: Boolean = false): Unit = {
    // lets try resolve the current project version
    version = findCurrentPomVersion
    logger.info("Looked up version from current pom: " + version)

    testScalateArchetype("org.fusesource.scalate.tooling", artifactId, version, testConsole)
  }

  protected def testScalateArchetype(groupId: String, artifactId: String, version: String, testConsole: Boolean): Unit = {
    logger.info("Attempting to create archetype: " + artifactId + " using version: " + version)

    // create a temp directory to run the archetype in
    var targetDir: File = new File(baseDir, "target/archetypes/" + artifactId)
    FileUtils.deleteDirectory(targetDir)
    targetDir.mkdirs
    //val createdArtifactId: String = UUID.randomUUID.toString
    val createdArtifactId: String = "myArtifact"

    var props: Properties = new Properties
    props.setProperty("archetypeGroupId", groupId)
    props.setProperty("archetypeArtifactId", artifactId)
    props.setProperty("archetypeVersion", version)
    props.setProperty("groupId", "sample")
    props.setProperty("artifactId", createdArtifactId)
    props.setProperty("user.dir", targetDir.getAbsolutePath)
    props.setProperty("basedir", targetDir.getAbsolutePath)

    var request: DefaultMavenExecutionRequest = new DefaultMavenExecutionRequest

    request.setSystemProperties(System.getProperties.clone.asInstanceOf[Properties])
    request.setUserProperties(props)
    request.setGoals(Collections.singletonList("archetype:generate"))
    request.setBaseDirectory(targetDir)
    request.setProjectPresent(false)
    runMaven(request)


    newProjectDir = new File(targetDir, createdArtifactId)
    logger.info("Now building created archetype in: " + newProjectDir)

    runMaven(mavenRequest(Collections.singletonList("install")))
/*
    request = new DefaultMavenExecutionRequest
    request.setSystemProperties(System.getProperties.clone.asInstanceOf[Properties])
    request.setGoals(Collections.singletonList("install"))
    request.setBaseDirectory(newProjectDir)
    request.setProjectPresent(true)
    request.setPom(new File(newProjectDir, "pom.xml"))
    runMaven(request)
*/


    if (testConsole) {
      // lets copy the ConsoleTest
      copyFile(baseDir.getParentFile.getParentFile.getPath +
              "/scalate-war/src/test/scala/org/fusesource/scalate/console/ConsoleTest.scala",
        newProjectDir.getPath + "/src/test/scala/org/fusesource/scalate/console/ConsoleTest.scala")

      // lets copy the TestGeneratedConsoleFiles
      copyFile(baseDir.getPath + "/src/test/scala/org/fusesource/scalate/console/TestGeneratedConsoleFiles.scala",
        newProjectDir.getPath + "/src/test/scala/org/fusesource/scalate/console/TestGeneratedConsoleFiles.scala")

      System.setProperty("scalate.package.resources", "sample.resources")
      System.setProperty("scalate.generate.src", new File(newProjectDir, "src").getPath)
      mavenTest("ConsoleTest")
      mavenTest("TestGeneratedConsoleFiles")
    }
  }


  def copyFile(fromFileName: String, toFileName: String): File = {
    val from  = new File(fromFileName)
    val to = new File(toFileName)
    println("copying from: " + from + " to: " + to)
    copy(from, to)
    to
  }

  def mavenTest(test: String): Unit = {
    val request = mavenRequest(Collections.singletonList("install"))
    val properties = new Properties()
    properties.put("test", test)
    request.setUserProperties(properties)
    runMaven(request)
  }
  def mavenRequest(goals: JList[String]): DefaultMavenExecutionRequest = {
    val request = new DefaultMavenExecutionRequest
    request.setSystemProperties(System.getProperties.clone.asInstanceOf[Properties])
    request.setGoals(goals)
    request.setBaseDirectory(newProjectDir)
    request.setProjectPresent(true)
    request.setPom(new File(newProjectDir, "pom.xml"))
    request
  }


  @Before
  def setUp: Unit = {
    container = createContainer
  }

  @After
  def tearDown: Unit = {
    if (container != null) {
      container.dispose
    }
    container = null
  }


  /**
   * Uses the current pom.xml to find the version
   */
  protected def findCurrentPomVersion: String = {
    val builder = container.lookup(classOf[ProjectBuilder])
    assert(builder != null)

    val buildingRequest = new DefaultProjectBuildingRequest
    buildingRequest.setOffline(false)

    var rsys = container.lookup(classOf[RepositorySystem])
    buildingRequest.setLocalRepository(rsys.createDefaultLocalRepository)
    buildingRequest.setRemoteRepositories(Collections.singletonList(rsys.createDefaultRemoteRepository))

    val buildingResult = builder.build(new File(baseDir, "pom.xml"), buildingRequest)
    assert(buildingResult != null)
    buildingResult.getProject.getVersion
  }

  protected def runMaven(request: MavenExecutionRequest): MavenExecutionResult = {
    assert(container != null)
    var maven = container.lookup(classOf[Maven])
    Assert.assertNotNull("Should have a maven!", maven)
    configureRequest(request)
    var result: MavenExecutionResult = null
    try {
      result = maven.execute(request)
    }
    finally {
      container.release(maven)
    }
    if (result.hasExceptions) {
      var handler: ExceptionHandler = new DefaultExceptionHandler
      var references: Map[String, String] = new LinkedHashMap[String, String]
      var project: MavenProject = null

      for (exception <- result.getExceptions) {
        var summary: ExceptionSummary = handler.handleException(exception)
        logSummary(summary, references, "", request.isShowErrors)
        if (project == null && exception.isInstanceOf[LifecycleExecutionException]) {
          project = (exception.asInstanceOf[LifecycleExecutionException]).getProject
        }
      }
      Assert.fail("Failed to invoke maven goals: " + request.getGoals + " due to exceptions: " + result.getExceptions)
    }
    return result
  }


  protected def configureRequest(request: MavenExecutionRequest): Unit = {
    assert(request != null)

    var settingsRequest: SettingsBuildingRequest = new DefaultSettingsBuildingRequest
    settingsRequest.setSystemProperties(request.getSystemProperties)
    settingsRequest.setUserProperties(request.getUserProperties)

    var settingsResult: SettingsBuildingResult = null
    val settingsBuilder = container.lookup(classOf[SettingsBuilder])
    try {
      settingsResult = settingsBuilder.build(settingsRequest)
    }
    finally {
      container.release(settingsBuilder)
    }
    val populator = container.lookup(classOf[MavenExecutionRequestPopulator])
    try {
      populator.populateFromSettings(request, settingsResult.getEffectiveSettings)
    }
    finally {
      container.release(populator)
    }
    val rsys = container.lookup(classOf[RepositorySystem])
    request.setLocalRepository(rsys.createDefaultLocalRepository)
    request.setRemoteRepositories(Collections.singletonList(rsys.createDefaultRemoteRepository))
    if (!settingsResult.getProblems.isEmpty && logger.isWarnEnabled) {
      logger.warn("")
      logger.warn("Some problems were encountered while building the effective settings")
      for (problem <- settingsResult.getProblems) {
        logger.warn(problem.getMessage + " @ " + problem.getLocation)
      }
      logger.warn("")
    }
    request.setShowErrors(true)
    request.setOffline(false)
    request.setInteractiveMode(false)
    request.setLoggingLevel(MavenExecutionRequest.LOGGING_LEVEL_DEBUG)
  }


  private def createContainer: DefaultPlexusContainer = {
    var cc: ContainerConfiguration = new DefaultContainerConfiguration
    cc.setName("maven")

    var c: DefaultPlexusContainer = new DefaultPlexusContainer(cc)
    configureContainer(c)
    return c
  }


  protected def configureContainer(c: DefaultPlexusContainer): Unit = {
    assert(c != null)
  }


  private def logSummary(summary: ExceptionSummary, references: Map[String, String], indent: String, showErrors: Boolean): Unit = {
    assert(summary != null)
    var referenceKey: String = ""
    if (org.codehaus.plexus.util.StringUtils.isNotEmpty(summary.getReference)) {
      referenceKey = references.get(summary.getReference)
      if (referenceKey == null) {
        referenceKey = "[Help " + (references.size + 1) + "]"
        references.put(summary.getReference, referenceKey)
      }
    }
    var msg: String = indent + summary.getMessage
    if (org.codehaus.plexus.util.StringUtils.isNotEmpty(referenceKey)) {
      if (msg.indexOf('\n') < 0) {
        msg += " -> " + referenceKey
      }
      else {
        msg += '\n' + indent + "-> " + referenceKey
      }
    }
    if (showErrors) {
      logger.error(msg, summary.getException)
    }
    else {
      logger.error(msg)
    }
    val childIndent = indent + "  "
    for (child <- summary.getChildren) {
      logSummary(child, references, childIndent, showErrors)
    }
  }
}