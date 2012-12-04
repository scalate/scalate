/**
 * Copyright (C) 2009-2011 the original author or authors.
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
package org.fusesource.scalate.maven;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
/**
 * This goal exports confluence mark-up out of a Confluence wiki and adds the files to
 * the resource target for Scalate to use in generating the site. Its guts are
 * copied from the ConfluenceExport command. This should be made more
 * modular.
 *
 * @author Eric Johnson, Fintan Bolton
 * @goal confexport
 * @phase generate-resources
 * @requiresProject
 * @requiresDependencyResolution test
 */
public class ConfExportMojo extends AbstractMojo {

    /**
     * @required
     * @readonly
     * @parameter expression="${project}"
     */
    MavenProject project;

    /**
     * Confluence base URL
     *
     * @parameter expression="${scalate.url}"
     */
    String url  = "https://cwiki.apache.org/confluence/";

    /**
     * The confluence space key
     *
     * @parameter expression="${scalate.space}"
     */
    String space = "XB";

    /**
     * The directory where the exported pages will land.
     *
     * @parameter expression="${project.build.directory}/${project.build.finalName}"
     */
    File target;

    /**
     * The Confluence username to access the wiki.
     *
     * @parameter expression="${scalate.user}"
     */
    String user;

    /**
     * The password used to access the wiki.
     *
     * @parameter expression="${scalate.password}"
     */
    String password;

    /**
     * Whether to allow spaces in filenames (boolean)
     *
     * @parameter expression="false"
     * @alias allow-spaces
     */
    String allow_spaces = "false";

    /**
     * The format of the downloaded pages. Possible values are: page and conf
     *
     * @parameter
     */
    String format = "page";

    /**
     * Generate a link database for DocBook.
     *
     * @parameter
     * @alias target-db
     */
    String target_db = "false";

    /**
     * Disable the confexport goal.
     *
     * @parameter expression="${scalate.confexport.skip}"
     */
    String skip = "false";

    /**
     * The test project classpath elements.
     *
     * @parameter expression="${project.testClasspathElements}"
     */
    List testClassPathElements;

    public void execute() throws MojoExecutionException, MojoFailureException {
        // Use reflection to invoke since the the scala support class is compiled after the java classes.
        try {
            Object o = getClass().getClassLoader().loadClass("org.fusesource.scalate.maven.ConfExportMojoSupport").newInstance();
            Method apply = o.getClass().getMethod("apply", new Class[]{ConfExportMojo.class});
            apply.invoke(o, this);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if( targetException instanceof MojoFailureException) {
                throw (MojoFailureException)targetException;
            }
            if( targetException instanceof MojoExecutionException) {
                throw (MojoExecutionException)targetException;
            }
            throw new MojoExecutionException("Unexpected failure.", e.getTargetException());
        } catch (Throwable e) {
            throw new MojoExecutionException("Unexpected failure.", e);
        }
    }
}
