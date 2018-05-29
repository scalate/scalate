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
package org.fusesource.scalate.japi;

import org.fusesource.scalate.Binding;
import org.fusesource.scalate.RenderContext;
import org.fusesource.scalate.TemplateEngine;
import org.fusesource.scalate.TemplateSource;
import org.fusesource.scalate.util.JavaInterops;
import scala.collection.mutable.Buffer;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A Java API facade over the Scala based {@link TemplateEngine} to make it a little easier to reuse from pure Java code.
 */
public class TemplateEngineFacade {
    private TemplateEngine engine;
    private String mode = "production";
    private List<File> sourceDirectories = new ArrayList<File>();
    private List<Binding> extraBindings = new ArrayList<Binding>();

    public String layout(String uri, Map<String, Object> attributes) {
        return getEngine().layout(uri, asScalaImmutableMap(attributes), asScalaExtraBindings());
    }

    public void layout(String uri, OutputStream out, Map<String, Object> attributes) {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        layout(uri, writer, attributes);
        writer.flush();
    }

    public void layout(String uri, PrintWriter out, Map<String, Object> attributes) {
        getEngine().layout(uri, out, asScalaImmutableMap(attributes));
    }

    public void layout(String uri, RenderContext context) {
        getEngine().layout(uri, context, asScalaExtraBindings());
    }

    public void layout(TemplateSource source, RenderContext context) {
        getEngine().layout(source, context, asScalaExtraBindings());
    }


    // Properties
    //-------------------------------------------------------------------------
    public TemplateEngine getEngine() {
        if (engine == null) {
            engine = createTemplateEngine();
        }
        return engine;
    }

    public void setEngine(TemplateEngine engine) {
        this.engine = engine;
    }

    public List<Binding> getExtraBindings() {
        return extraBindings;
    }

    public void setExtraBindings(List<Binding> extraBindings) {
        this.extraBindings = extraBindings;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public List<File> getSourceDirectories() {
        return sourceDirectories;
    }

    public void setSourceDirectories(List<File> sourceDirectories) {
        this.sourceDirectories = sourceDirectories;
    }


    // Implementation methods
    //-------------------------------------------------------------------------
    protected Buffer<Binding> asScalaExtraBindings() {
        return Converter.asScalaBuffer(getExtraBindings());
    }

    protected scala.collection.immutable.Map<String, Object> asScalaImmutableMap(Map<String, Object> attributes) {
        scala.collection.mutable.Map<String, Object> mutableAttributes = Converter.mapAsScalaMap(attributes);
        return JavaInterops.toImmutableMap(mutableAttributes);
    }

    protected TemplateEngine createTemplateEngine() {
        Buffer<File> scalaSourceDirectories = Converter.asScalaBuffer(sourceDirectories);
        return TemplateEngine.apply(scalaSourceDirectories, mode);
    }
}
