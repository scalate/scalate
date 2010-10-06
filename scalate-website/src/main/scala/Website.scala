import org.fusesource.scalate.RenderContext

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
package

/**
 * <p>
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
object Website {

  val project_name= "Scalate"
  val project_slogan= "Scala Template Engine: like JSP without the crap but with added Scala coolness"
  val project_id= "scalate"
  val project_jira_key= "SCALATE"
  val project_issue_url= "http://scalate.assembla.com/spaces/scalate/support/tickets"
  val project_forums_url= "http://scalate.fusesource.org/community.html"
  val project_wiki_url= "http://wiki.github.com/scalate/scalate/"
  val project_logo= "/images/project-logo.png"
  val project_version= "1.3"
  val project_snapshot_version= "1.4-SNAPSHOT"

  val github_page= "http://github.com/scalate/scalate"
  val git_user_url= "git://github.com/scalate/scalate.git"
  val git_commiter_url= "git@github.com:scalate/scalate.git"

  // -------------------------------------------------------------------
  val project_svn_url= "http://fusesource.com/forge/svn/%s/trunk".format(project_id)
  val project_svn_branches_url= "http://fusesource.com/forge/svn/%s/branches".format(project_id)
  val project_svn_tags_url= "http://fusesource.com/forge/svn/%s/tags".format(project_id)
  val project_maven_groupId= "org.fusesource.%s".format(project_id)
  val project_maven_artifactId= "scalate-core"

}