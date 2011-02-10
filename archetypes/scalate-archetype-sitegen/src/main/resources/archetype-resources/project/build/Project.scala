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
import sbt._
import org.fusesource.scalate.sbt._

class Project(info: ProjectInfo) extends DefaultWebProject(info) with SiteGenWebProject {

  lazy val fusesource_snapshot_repo = "FuseSource Snapshots" at
           "http://repo.fusesource.com/nexus/content/repositories/snapshots"

  lazy val scalate_core     = "org.fusesource.scalate" % "scalate-core"     % "${project.version}" 
  lazy val scalate_wikitext = "org.fusesource.scalate" % "scalate-wikitext" % "${project.version}" 
  lazy val scalate_page     = "org.fusesource.scalate" % "scalate-page"     % "${project.version}" 
  lazy val scalamd          = "org.fusesource.scalamd" % "scalamd"          % "${scalamd-version}" 
  lazy val slf4j            = "org.slf4j"              % "slf4j-nop"        % "${slf4j-version}"

  // to get jetty-run working in sbt
  lazy val jetty_webapp     = "org.eclipse.jetty"      % "jetty-webapp"     % "7.0.2.RC0" % "test"

}
