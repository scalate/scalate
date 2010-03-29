/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.scalate.tooling;

import org.apache.maven.Maven;
import org.apache.maven.exception.DefaultExceptionHandler;
import org.apache.maven.exception.ExceptionHandler;
import org.apache.maven.exception.ExceptionSummary;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.apache.maven.settings.building.SettingsProblem;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public abstract class AbstractArchetypeTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractArchetypeTest.class);

    private static final File baseDir = new File(System.getProperty("basedir", ".")).getAbsoluteFile();
    private DefaultPlexusContainer container;
    protected String version = "1.0-SNAPSHOT"; // TODO read from POM!


    protected void testScalateArchetype(String artifactId) throws Exception {
        testArchetype("org.fusesource.scalate.tooling", artifactId, version);
    }

    protected void testArchetype(String groupId, String artifactId, String version) throws Exception {
        System.out.println("Attempting to create archetype: " + artifactId);

        File targetDir = new File(baseDir, "target/archetypes/" + artifactId);
        FileUtils.deleteDirectory(targetDir);
        targetDir.mkdirs();
/*
        EventMonitor eventMonitor = new DefaultEventMonitor(new PlexusLoggerAdapter(
                        new MavenEmbedderConsoleLogger()));
*/

        Properties props = new Properties();
        props.setProperty("archetypeGroupId", groupId);
        props.setProperty("archetypeArtifactId", artifactId);
        props.setProperty("archetypeVersion", version);
        props.setProperty("groupId", "sample");
        String createdArtifactId = UUID.randomUUID().toString();
        props.setProperty("artifactId", createdArtifactId);
        props.setProperty("user.dir", targetDir.getAbsolutePath());
        props.setProperty("basedir", targetDir.getAbsolutePath());



        DefaultMavenExecutionRequest request = new DefaultMavenExecutionRequest();

        request.setSystemProperties((Properties) System.getProperties().clone());
        request.setUserProperties(props);
        request.setGoals(Collections.singletonList("archetype:create"));
        request.setBaseDirectory(targetDir);
        request.setProjectPresent(false);

        runMaven(request);


        File newProjectDir = new File(targetDir, createdArtifactId);
        System.out.println("Now building created archetype in: " + newProjectDir);

        request = new DefaultMavenExecutionRequest();

        request.setSystemProperties((Properties) System.getProperties().clone());
        //request.setUserProperties(props);
        request.setGoals(Collections.singletonList("install"));
        request.setBaseDirectory(newProjectDir);
        request.setProjectPresent(true);
        request.setPom(new File(newProjectDir, "pom.xml"));

        runMaven(request);
    }


    @Before
    public void setUp() throws Exception {
        container = createContainer();
    }

    @After
    public void tearDown() throws Exception {
        if (container != null) {
            container.dispose();
        }
        container = null;
    }

    protected MavenExecutionResult runMaven(MavenExecutionRequest request) throws Exception {

        Maven maven = container.lookup(Maven.class);
        Assert.assertNotNull("Should have a maven!", maven);

/*
        TODO
        
        MavenProject project = maven.readProject(new File(baseDir, "pom.xml"));
        version = project.getVersion();
*/
        configureRequest(request);


        MavenExecutionResult result;
        try {
            result = maven.execute(request);
        }
        finally {
            container.release(maven);
        }

        System.out.println("Got result: " + result);

        if (result.hasExceptions()) {
            // else process exceptions

            ExceptionHandler handler = new DefaultExceptionHandler();
            Map<String, String> references = new LinkedHashMap<String, String>();
            MavenProject project = null;


            for (Throwable exception : result.getExceptions()) {
                System.out.println("Exception: " + exception);

                ExceptionSummary summary = handler.handleException(exception);

                System.out.println("summary: " + summary);

                logSummary(summary, references, "", request.isShowErrors());

                if (project == null && exception instanceof LifecycleExecutionException) {
                    project = ((LifecycleExecutionException) exception).getProject();
                }
            }

            Assert.fail("Failed to invoke maven goals: " + request.getGoals() + " due to exceptions: " + result.getExceptions());
        }

        return result;
    }

    protected void configureRequest(MavenExecutionRequest request) throws Exception {
        assert request != null;

        SettingsBuildingRequest settingsRequest = new DefaultSettingsBuildingRequest()
/*
            .setGlobalSettingsFile(globalSettingsFile)
            .setUserSettingsFile(userSettingsFile)
*/
            .setSystemProperties(request.getSystemProperties())
            .setUserProperties(request.getUserProperties());

        SettingsBuildingResult settingsResult;
        SettingsBuilder settingsBuilder = container.lookup(SettingsBuilder.class);
        try {
            settingsResult = settingsBuilder.build(settingsRequest);
        }
        finally {
            container.release(settingsBuilder);
        }

        // NOTE: This will nuke some details from the request; profiles, online, etc... :-(
        MavenExecutionRequestPopulator populator = container.lookup(MavenExecutionRequestPopulator.class);
        try {
            populator.populateFromSettings(request, settingsResult.getEffectiveSettings());
        }
        finally {
            container.release(populator);
        }


        RepositorySystem rsys = container.lookup(RepositorySystem.class);
        request.setLocalRepository(rsys.createDefaultLocalRepository());
        request.setRemoteRepositories(Collections.singletonList(rsys.createDefaultRemoteRepository()));
        
        if (!settingsResult.getProblems().isEmpty() && logger.isWarnEnabled()) {
            logger.warn("");
            logger.warn("Some problems were encountered while building the effective settings"); // TODO: i18n

            for (SettingsProblem problem : settingsResult.getProblems()) {
                logger.warn(problem.getMessage() + " @ " + problem.getLocation()); // TODO: i18n
            }

            logger.warn("");
        }

        request.setShowErrors(true);
        request.setOffline(false);
        request.setInteractiveMode(false);
        request.setLoggingLevel(MavenExecutionRequest.LOGGING_LEVEL_DEBUG);
    }


    private DefaultPlexusContainer createContainer() throws Exception {
        ContainerConfiguration cc = new DefaultContainerConfiguration()
                //.setClassWorld(config.getClassWorld())
                .setName("maven");

        DefaultPlexusContainer c = new DefaultPlexusContainer(cc);
        configureContainer(c);

        return c;
    }

    protected void configureContainer(final DefaultPlexusContainer c) throws Exception {
        assert c != null;

/*
        c.setLoggerManager(new MavenLoggerManager(config.getLogger()));
        c.getLoggerManager().setThresholds(logger.getThreshold());

        // If there is a configuration delegate then call it
        if (config.getDelegate() != null) {
            config.getDelegate().configure(c);
        }
*/
    }

    private void logSummary(final ExceptionSummary summary, final Map<String, String> references, String indent, final boolean showErrors) {
        assert summary != null;

        String referenceKey = "";

        // TODO: i18n

        if (org.codehaus.plexus.util.StringUtils.isNotEmpty(summary.getReference())) {
            referenceKey = references.get(summary.getReference());
            if (referenceKey == null) {
                referenceKey = "[Help " + (references.size() + 1) + "]";
                references.put(summary.getReference(), referenceKey);
            }
        }

        String msg = indent + summary.getMessage();

        if (org.codehaus.plexus.util.StringUtils.isNotEmpty(referenceKey)) {
            if (msg.indexOf('\n') < 0) {
                msg += " -> " + referenceKey;
            } else {
                msg += '\n' + indent + "-> " + referenceKey;
            }
        }

        if (showErrors) {
            //noinspection ThrowableResultOfMethodCallIgnored
            logger.error(msg, summary.getException());
        } else {
            logger.error(msg);
        }

        indent += "  ";

        for (ExceptionSummary child : summary.getChildren()) {
            logSummary(child, references, indent, showErrors);
        }
    }
}
