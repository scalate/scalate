package org.fusesource.scalate.tooling

import _root_.org.apache.maven.project.{DefaultProjectBuildingRequest, ProjectBuildingRequest, ProjectBuilder, MavenProject}
import _root_.org.junit.{After, Before, Assert}
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
import java.util.LinkedHashMap
import java.util.Map
import java.util.Properties
import java.util.UUID
import scala.collection.JavaConversions._

/**
 * @version $Revision : 1.1 $
 */

class ArchetypeTestSupport {
  protected val logger = LoggerFactory.getLogger(getClass)
  protected var baseDir = new File(System.getProperty("basedir", ".")).getAbsoluteFile
  protected var container: DefaultPlexusContainer = null

  // TODO discover this from the pom!
  protected var version: String = _

  protected def testScalateArchetype(artifactId: String): Unit = {
    // lets try resolve the current project version
    version = findCurrentPomVersion
    logger.info("Looked up version from current pom: " + version)

    testArchetype("org.fusesource.scalate.tooling", artifactId, version)
  }

  protected def testArchetype(groupId: String, artifactId: String, version: String): Unit = {
    System.out.logger.info("Attempting to create archetype: " + artifactId + " using version: " + version)

    // create a temp directory to run the archetype in
    var targetDir: File = new File(baseDir, "target/archetypes/" + artifactId)
    FileUtils.deleteDirectory(targetDir)
    targetDir.mkdirs
    var createdArtifactId: String = UUID.randomUUID.toString

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
    request.setGoals(Collections.singletonList("archetype:create"))
    request.setBaseDirectory(targetDir)
    request.setProjectPresent(false)
    runMaven(request)


    var newProjectDir: File = new File(targetDir, createdArtifactId)
    System.out.logger.info("Now building created archetype in: " + newProjectDir)

    request = new DefaultMavenExecutionRequest
    request.setSystemProperties(System.getProperties.clone.asInstanceOf[Properties])
    request.setGoals(Collections.singletonList("install"))
    request.setBaseDirectory(newProjectDir)
    request.setProjectPresent(true)
    request.setPom(new File(newProjectDir, "pom.xml"))
    runMaven(request)
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