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
import java.util.ArrayList;

/**
 * This goal precompiles the Scalate templates into classes to be included
 * in your build.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 *
 * @goal precompile
 * @phase prepare-package
 * @requiresProject
 * @requiresDependencyResolution test
 */
public class PrecompileMojo extends AbstractMojo {

    /**
     * @required
     * @readonly
     * @parameter expression="${project}"
     */
    MavenProject project;

    /**
     * The directory where the templates files are located.
     *
     * @parameter expression="${basedir}/src/main/webapp"
     */
    File warSourceDirectory;

    /**
     * The directory where resources are located.
     *
     * @parameter expression="${basedir}/src/main/resources"
     */
    File resourcesSourceDirectory;

    /**
     * The directory where the scala code will be generated into.
     *
     * @parameter expression="${project.build.directory}/generated-sources/scalate"
     */
    File targetDirectory;

    /**
     * The directory containing generated classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     */
    File classesDirectory;

    /**
     * Additional template paths to compile.
     *
     * @parameter
     */
    ArrayList<String> templates = new ArrayList<String>();

    /**
     * The class name of the render context.
     *
     * @parameter
     */
    String contextClass;

    /**
     * The class name of the Boot class to use.
     *
     * @parameter
     */
    String bootClassName;

    /**
     * The test project classpath elements.
     *
     * @parameter expression="${project.testClasspathElements}"
     */
    java.util.List classPathElements;


    public void execute() throws MojoExecutionException, MojoFailureException {
        // Use reflection to invoke since the the scala support class is compiled after the java classes.
        try {
            Object o = getClass().getClassLoader().loadClass("org.fusesource.scalate.maven.PrecompileMojoSupport").newInstance();
            Method apply = o.getClass().getMethod("apply", new Class[]{PrecompileMojo.class});
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
