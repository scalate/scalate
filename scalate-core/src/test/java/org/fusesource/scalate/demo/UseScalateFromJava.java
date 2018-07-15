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
package org.fusesource.scalate.demo;

import org.fusesource.scalate.japi.TemplateEngineFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Shows how to use Scalate from a Java class
 */
public class UseScalateFromJava extends TemplateEngineFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(UseScalateFromJava.class);

    public static class LoggingOutputStream extends OutputStream {

        private final ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
        private final Logger logger;
        private final LogLevel level;

        public enum LogLevel {
            TRACE, DEBUG, INFO, WARN, ERROR,
        }

        public LoggingOutputStream(Logger logger, LogLevel level) {
            this.logger = logger;
            this.level = level;
        }

        @Override
        public void write(int b) {
            if (b == '\n') {
                String line = baos.toString();
                baos.reset();

                switch (level) {
                    case TRACE:
                        logger.trace(line);
                        break;
                    case DEBUG:
                        logger.debug(line);
                        break;
                    case ERROR:
                        logger.error(line);
                        break;
                    case INFO:
                        logger.info(line);
                        break;
                    case WARN:
                        logger.warn(line);
                        break;
                }
            } else {
                baos.write(b);
            }
        }

    }

    public static void main(String[] args) {
        if (args.length <= 0) {
            LOGGER.info("Usage: UseScalateFromJava templateUri");
            System.exit(1);
        }
        TemplateEngineFacade engine = new TemplateEngineFacade();

        Map<String,Object> attributes = new HashMap<>();
        attributes.put("name", "James Strachan");

        OutputStream out = new LoggingOutputStream(LOGGER, LoggingOutputStream.LogLevel.INFO);
        engine.layout(args[0], out, attributes);
    }

}
