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
package org.fusesource.scalate.support;

import org.fusesource.scalate.TemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A pure Java Boot class
 */
public class MyJavaBoot {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyJavaBoot.class);

    public static int runCount = 0;

    private final TemplateEngine engine;

    public MyJavaBoot(TemplateEngine engine) {
        this.engine = engine;
    }

    public void run() {
        LOGGER.info("Running MyJavaBoot");
        if (engine == null) {
            throw new NullPointerException("Should have a template engine!");
        }
        runCount++;
    }
}
