package org.fusesource.scalate.maven;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.doxia.linkcheck.HttpBean;
import org.apache.maven.doxia.linkcheck.LinkCheck;
import org.apache.maven.doxia.linkcheck.LinkCheckException;
import org.apache.maven.doxia.linkcheck.model.LinkcheckFile;
import org.apache.maven.doxia.linkcheck.model.LinkcheckFileResult;
import org.apache.maven.doxia.linkcheck.model.LinkcheckModel;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.i18n.I18N;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.WriterFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Generates a <code>Linkcheck</code> report.
 *
 * Based on code from the maven-linkcheck-plugin
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @goal linkcheck
 */
public class LinkcheckReport
    extends AbstractMavenReport
{
    // ----------------------------------------------------------------------
    // Report Components
    // ----------------------------------------------------------------------

    /**
     * Internationalization.
     *
     * @component
     */
    private I18N i18n;

    /**
     * Doxia Site Renderer.
     *
     * @component
     */
    private Renderer siteRenderer;

    /**
     * LinkCheck component.
     *
     * @component
     */
    private LinkCheck linkCheck;

    // ----------------------------------------------------------------------
    // Report Parameters
    // ----------------------------------------------------------------------

    /**
     * The Maven Project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Local Repository.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * Report output directory.
     *
     * @parameter expression="${project.reporting.outputDirectory}"
     * @required
     */
    private File outputDirectory;

    /**
     * The Maven Settings.
     *
     * @parameter default-value="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;

    // ----------------------------------------------------------------------
    // Linkcheck parameters
    // ----------------------------------------------------------------------

    /**
     * Whether we are offline or not.
     *
     * @parameter default-value="${settings.offline}" expression="${linkcheck.offline}"
     * @required
     */
    private boolean offline;

    /**
     * If online, the HTTP method should automatically follow HTTP redirects,
     * <tt>false</tt> otherwise.
     *
     * @parameter default-value="true"
     */
    private boolean httpFollowRedirect;

    /**
     * The location of the Linkcheck cache file.
     *
     * @parameter default-value="${project.build.directory}/linkcheck/linkcheck.cache"
     * @required
     */
    protected File linkcheckCache;

    /**
     * The location of the Linkcheck report file.
     *
     * @parameter default-value="${project.build.directory}/linkcheck/linkcheck.xml"
     * @required
     */
    protected File linkcheckOutput;

    /**
     * The HTTP method to use. Currently supported are "GET" and "HEAD".
     * <dl>
     * <dt>HTTP GET</dt>
     * <dd>
     * The HTTP GET method is defined in section 9.3 of
     * <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC2616</a>:
     * The GET method means retrieve whatever information (in the form of an
     * entity) is identified by the Request-URI.
     * </dd>
     * <dt>HTTP HEAD</dt>
     * <dd>
     * The HTTP HEAD method is defined in section 9.4 of
     * <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC2616</a>:
     * The HEAD method is identical to GET except that the server MUST NOT
     * return a message-body in the response.
     * </dd>
     * </dl>
     *
     * @parameter default-value="head"
     * @required
     */
    private String httpMethod;

    /**
     * The list of HTTP errors to ignored, like <code>404</code>.
     *
     * @parameter
     * @see {@link org.apache.commons.httpclient.HttpStatus} for all defined values.
     */
    private int[] excludedHttpStatusErrors;

    /**
     * The list of HTTP warnings to ignored, like <code>301</code>.
     *
     * @parameter
     * @see {@link org.apache.commons.httpclient.HttpStatus} for all defined values.
     */
    private int[] excludedHttpStatusWarnings;

    /**
     * The list of site pages to exclude. By default, this report, i.e. <code>linkcheck.html</code>, will be excluded.
     * <br/>
     * <b>Note</b>: No pattern is allowed for excludedPage, only specific file names.
     *
     * @parameter
     */
    private String[] excludedPages;

    /**
     * The list of links to exclude.
     * <br/>
     * <b>Note</b>: Patterns like <code>&#42;&#42;/dummy/&#42;</code> are allowed for excludedLink.
     *
     * @parameter
     */
    private String[] excludedLinks;

    /**
     * The file encoding to use when Linkcheck reads the source files. If the property
     * <code>project.build.sourceEncoding</code> is not set, the platform default encoding is used.
     *
     * @parameter expression="${encoding}" default-value="${project.build.sourceEncoding}"
     */
    private String encoding;

    /**
     * The extra HttpClient parameters to be used when fetching links. For instance:
     * <pre>
     * &lt;httpClientParameters&gt;
     * &nbsp;&lt;property&gt;
     * &nbsp;&nbsp;&lt;name&gt;http.protocol.max-redirects&lt;/name&gt;
     * &nbsp;&nbsp;&lt;value&gt;10&lt;/value&gt;
     * &nbsp;&lt;/property&gt;
     * &lt;/httpClientParameters&gt;
     * </pre>
     * See <a href="http://hc.apache.org/httpclient-3.x/preference-api.html">HttpClient preference page</a>
     *
     * @parameter expression="${httpClientParameters}"
     */
    private Properties httpClientParameters;

    /**
     * Set the timeout to be used when fetching links. A value of zero means the timeout is not used.
     *
     * @parameter expression="${timeout}" default-value="2000"
     */
    private int timeout;

    /**
     * <code>true</code> to skip the report execution, <code>false</code> otherwise.
     * The purpose is to prevent infinite call when {@link #forceSite} is enable.
     *
     * @parameter expression="${linkcheck.skip}" default-value="false"
     */
    private boolean skip;

    /**
     * <code>true</code> to force the site generation, <code>false</code> otherwise.
     * Using this parameter ensures that all documents have been correctly generated.
     *
     * @parameter expression="${linkcheck.forceSite}" default-value="true"
     */
    private boolean forceSite;

    /**
     * The base URL to use for absolute links (eg <code>/index.html</code>) in the site.
     *
     * @parameter expression="${linkcheck.baseURL}" default-value="${project.url}"
     */
    private String baseURL;

    /**
     * The base directory of the generated site to link check
     *
     * @parameter default-value="${project.build.directory}/sitegen"
     */
    private File basedir;

    // ----------------------------------------------------------------------
    // Instance fields
    // ----------------------------------------------------------------------

    /** Result of the linkcheck in {@link #execute()} */
    private LinkcheckModel result;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public String getDescription( Locale locale )
    {
        return i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.description" );
    }

    /** {@inheritDoc} */
    public String getName( Locale locale )
    {
        return i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.name" );
    }

    /** {@inheritDoc} */
    public String getOutputName()
    {
        return "sitegen-linkcheck";
    }

    /** {@inheritDoc} */
    public boolean canGenerateReport()
    {
        if ( skip )
        {
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    public void execute()
        throws MojoExecutionException
    {
        if ( !canGenerateReport() )
        {
            return;
        }

        // encoding
        if ( StringUtils.isEmpty( encoding ) )
        {
            if ( getLog().isWarnEnabled() )
            {
                getLog().warn(
                               "File encoding has not been set, using platform encoding "
                                   + ReaderFactory.FILE_ENCODING + ", i.e. build is platform dependent!" );
            }
            encoding = ReaderFactory.FILE_ENCODING;
        }

        List documents = null;
        try
        {
            documents = FileUtils.getFiles( basedir, "**/*.html", null );
        }
        catch ( IOException e )
        {
            String msg = "IOException: " + e.getMessage();
            if ( getLog().isDebugEnabled() )
            {
                getLog().error( msg, e );
            }
            else
            {
                getLog().error( msg );
            }
        }

        // if the site was not already generated, invoke it
        if ( documents == null || ( documents != null && documents.size() == 0 ) )
        {
            throw new MojoExecutionException( "No site generated yet. Please invoke the scalate:sitegen plugin" );
        }

        try
        {
            result = executeLinkCheck( basedir );
        }
        catch ( LinkCheckException e )
        {
            throw new MojoExecutionException( "LinkCheckException: " + e.getMessage(), e );
        }
    }

    // ----------------------------------------------------------------------
    // Protected methods
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    protected String getOutputDirectory()
    {
        return outputDirectory.getAbsolutePath();
    }

    /** {@inheritDoc} */
    protected MavenProject getProject()
    {
        return project;
    }

    /** {@inheritDoc} */
    protected Renderer getSiteRenderer()
    {
        return siteRenderer;
    }

    /** {@inheritDoc} */
    protected void executeReport( Locale locale )
        throws MavenReportException
    {
        if ( result == null )
        {
            getLog().debug( "Calling execute()" );

            try
            {
                this.execute();
            }
            catch ( MojoExecutionException e )
            {
                throw new MavenReportException( "MojoExecutionException: " + e.getMessage(), e );
            }
        }

        if ( result != null )
        {
            generateReport( locale, result );
            // free memory
            result = null;
        }
    }

    // ----------------------------------------------------------------------
    // Private methods
    // ----------------------------------------------------------------------

    /**
     * Execute the <code>Linkcheck</code> tool.
     *
     * @param basedir not null
     * @throws LinkCheckException if any
     */
    private LinkcheckModel executeLinkCheck( File basedir )
        throws LinkCheckException
    {
        // Wrap linkcheck
        linkCheck.setOnline( !offline );
        linkCheck.setBasedir( basedir );
        linkCheck.setBaseURL( baseURL );
        linkCheck.setReportOutput( linkcheckOutput );
        linkCheck.setLinkCheckCache( linkcheckCache );
        linkCheck.setExcludedLinks( excludedLinks );
        linkCheck.setExcludedPages( getExcludedPages() );
        linkCheck.setExcludedHttpStatusErrors( excludedHttpStatusErrors );
        linkCheck.setExcludedHttpStatusWarnings( excludedHttpStatusWarnings );
        linkCheck.setEncoding( ( StringUtils.isNotEmpty( encoding ) ? encoding : WriterFactory.UTF_8 ) );

        HttpBean bean = new HttpBean();
        bean.setMethod( httpMethod );
        bean.setFollowRedirects( httpFollowRedirect );
        bean.setTimeout( timeout );
        if ( httpClientParameters != null )
        {
            bean.setHttpClientParameters( httpClientParameters );
        }

        Proxy proxy = settings.getActiveProxy();
        if ( proxy != null )
        {
            bean.setProxyHost( proxy.getHost() );
            bean.setProxyPort( proxy.getPort() );
            bean.setProxyUser( proxy.getUsername() );
            bean.setProxyPassword( proxy.getPassword() );
        }
        linkCheck.setHttp( bean );

        return linkCheck.execute();
    }

    /**
     * @return the excludedPages defined by the user and also this report.
     */
    private String[] getExcludedPages()
    {
        List pagesToExclude =
            ( excludedPages != null ? new ArrayList( Arrays.asList( excludedPages ) ) : new ArrayList() );

        // Exclude this report
        pagesToExclude.add( getOutputName() + ".html" );

        return (String[]) pagesToExclude.toArray( new String[0] );
    }


    // ----------------------------------------------------------------------
    // Linkcheck report
    // ----------------------------------------------------------------------

    private void generateReport( Locale locale, LinkcheckModel linkcheckModel )
    {
        getSink().head();
        getSink().title();
        getSink().text( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.title" ) );
        getSink().title_();
        getSink().head_();

        getSink().body();

        if ( linkcheckModel == null )
        {
            getSink().section1();
            getSink().sectionTitle1();
            getSink().text( getName( locale ) );
            getSink().sectionTitle1_();

            getSink().paragraph();
            getSink().rawText( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.empty" ) );
            getSink().paragraph_();

            getSink().section1_();

            getSink().body_();
            getSink().flush();
            getSink().close();

            return;
        }

        // Overview
        getSink().section1();
        getSink().sectionTitle1();
        getSink().text( getName( locale ) );
        getSink().sectionTitle1_();

        getSink().paragraph();
        getSink().rawText( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.overview" ) );
        getSink().paragraph_();

        getSink().section1_();

        // Statistics
        generateSummarySection( locale, linkcheckModel );

        if ( linkcheckModel.getFiles().size() > 0 )
        {
            // Details
            generateDetailsSection( locale, linkcheckModel );
        }

        getSink().body_();
        getSink().flush();
        getSink().close();

        closeReport();
    }

    private void generateSummarySection( Locale locale, LinkcheckModel linkcheckModel )
    {
        // Calculus
        List linkcheckFiles = linkcheckModel.getFiles();

        int totalFiles = linkcheckFiles.size();

        int totalLinks = 0;
        int totalValidLinks = 0;
        int totalErrorLinks = 0;
        int totalWarningLinks = 0;
        for ( Iterator it = linkcheckFiles.iterator(); it.hasNext(); )
        {
            LinkcheckFile linkcheckFile = (LinkcheckFile) it.next();

            totalLinks += linkcheckFile.getNumberOfLinks();
            totalValidLinks += linkcheckFile.getNumberOfLinks( LinkcheckFileResult.VALID_LEVEL );
            totalErrorLinks += linkcheckFile.getNumberOfLinks( LinkcheckFileResult.ERROR_LEVEL );
            totalWarningLinks += linkcheckFile.getNumberOfLinks( LinkcheckFileResult.WARNING_LEVEL );
        }

        getSink().section1();
        getSink().sectionTitle1();
        getSink().text( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.summary" ) );
        getSink().sectionTitle1_();

        // Summary of the analysis parameters
        getSink().paragraph();
        getSink().rawText( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.summary.overview1" ) );
        getSink().paragraph_();

        getSink().table();

        getSink().tableRow();
        getSink().tableHeaderCell();
        getSink().text( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.summary.table.parameter" ) );
        getSink().tableHeaderCell_();
        getSink().tableHeaderCell();
        getSink().text( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.summary.table.value" ) );
        getSink().tableHeaderCell_();
        getSink().tableRow_();

        getSink().tableRow();
        getSink().tableCell();
        getSink().rawText(
                           i18n.getString( "sitegen-linkcheck-report", locale,
                                           "report.sitegen.linkcheck.summary.table.httpFollowRedirect" ) );
        getSink().tableCell_();
        getSink().tableCell();
        getSink().text( String.valueOf( httpFollowRedirect ) );
        getSink().tableCell_();
        getSink().tableRow_();

        getSink().tableRow();
        getSink().tableCell();
        getSink()
                 .rawText(
                           i18n
                               .getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.summary.table.httpMethod" ) );
        getSink().tableCell_();
        getSink().tableCell();
        if ( StringUtils.isEmpty( httpMethod ) )
        {
            getSink().text( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.summary.table.none" ) );
        }
        else
        {
            getSink().text( httpMethod );
        }
        getSink().tableCell_();
        getSink().tableRow_();

        getSink().tableRow();
        getSink().tableCell();
        getSink().rawText(
                           i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.summary.table.offline" ) );
        getSink().tableCell_();
        getSink().tableCell();
        getSink().text( String.valueOf( offline ) );
        getSink().tableCell_();
        getSink().tableRow_();

        getSink().tableRow();
        getSink().tableCell();
        getSink().rawText(
                           i18n.getString( "sitegen-linkcheck-report", locale,
                                           "report.sitegen.linkcheck.summary.table.excludedPages" ) );
        getSink().tableCell_();
        getSink().tableCell();
        if ( getExcludedPages() == null || getExcludedPages().length == 0 )
        {
            getSink().text( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.summary.table.none" ) );
        }
        else
        {
            getSink().text( StringUtils.join( getExcludedPages(), "," ) );
        }
        getSink().tableCell_();
        getSink().tableRow_();

        getSink().tableRow();
        getSink().tableCell();
        getSink().rawText(
                           i18n.getString( "sitegen-linkcheck-report", locale,
                                           "report.sitegen.linkcheck.summary.table.excludedLinks" ) );
        getSink().tableCell_();
        getSink().tableCell();
        if ( excludedLinks == null || excludedLinks.length == 0 )
        {
            getSink().text( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.summary.table.none" ) );
        }
        else
        {
            getSink().text( StringUtils.join( excludedLinks, "," ) );
        }
        getSink().tableCell_();
        getSink().tableRow_();

        getSink().tableRow();
        getSink().tableCell();
        getSink().rawText(
                           i18n.getString( "sitegen-linkcheck-report", locale,
                                           "report.sitegen.linkcheck.summary.table.excludedHttpStatusErrors" ) );
        getSink().tableCell_();
        getSink().tableCell();
        if ( excludedHttpStatusErrors == null || excludedHttpStatusErrors.length == 0 )
        {
            getSink().text( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.summary.table.none" ) );
        }
        else
        {
            getSink().text( toString( excludedHttpStatusErrors ) );
        }
        getSink().tableCell_();
        getSink().tableRow_();

        getSink().tableRow();
        getSink().tableCell();
        getSink().rawText(
                           i18n.getString( "sitegen-linkcheck-report", locale,
                                           "report.sitegen.linkcheck.summary.table.excludedHttpStatusWarnings" ) );
        getSink().tableCell_();
        getSink().tableCell();
        if ( excludedHttpStatusWarnings == null || excludedHttpStatusWarnings.length == 0 )
        {
            getSink().text( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.summary.table.none" ) );
        }
        else
        {
            getSink().text( toString( excludedHttpStatusWarnings ) );
        }
        getSink().tableCell_();
        getSink().tableRow_();

        getSink().table_();

        // Summary of the checked files
        getSink().paragraph();
        getSink().rawText( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.summary.overview2" ) );
        getSink().paragraph_();

        getSink().table();

        // Header
        generateTableHeader( locale, false );

        // Content
        getSink().tableRow();

        getSink().tableCell();
        getSink().bold();
        getSink().text( totalFiles + "" );
        getSink().bold_();
        getSink().tableCell_();
        getSink().tableCell();
        getSink().bold();
        getSink().text( totalLinks + "" );
        getSink().bold_();
        getSink().tableCell_();
        getSink().tableCell();
        getSink().bold();
        getSink().text( String.valueOf( totalValidLinks ) );
        getSink().bold_();
        getSink().tableCell_();
        getSink().tableCell();
        getSink().bold();
        getSink().text( String.valueOf( totalWarningLinks ) );
        getSink().bold_();
        getSink().tableCell_();
        getSink().tableCell();
        getSink().bold();
        getSink().text( String.valueOf( totalErrorLinks ) );
        getSink().bold_();
        getSink().tableCell_();

        getSink().tableRow_();

        getSink().table_();

        getSink().section1_();
    }

    private void generateDetailsSection( Locale locale, LinkcheckModel linkcheckModel )
    {
        getSink().section1();
        getSink().sectionTitle1();
        getSink().text( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.detail" ) );
        getSink().sectionTitle1_();

        getSink().paragraph();
        getSink().rawText( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.detail.overview" ) );
        getSink().paragraph_();

        getSink().table();

        // Header
        generateTableHeader( locale, true );

        // Content
        List linkcheckFiles = linkcheckModel.getFiles();
        for ( Iterator it = linkcheckFiles.iterator(); it.hasNext(); )
        {
            LinkcheckFile linkcheckFile = (LinkcheckFile) it.next();

            getSink().tableRow();

            getSink().tableCell();
            if ( linkcheckFile.getUnsuccessful() == 0 )
            {
                iconValid( locale );
            }
            else
            {
                iconError( locale );
            }
            getSink().tableCell_();

            // tableCell( createLinkPatternedText( linkcheckFile.getRelativePath(), "./"
            // + linkcheckFile.getRelativePath() ) );
            getSink().tableCell();
            getSink().link( linkcheckFile.getRelativePath() );
            getSink().text( linkcheckFile.getRelativePath() );
            getSink().link_();
            getSink().tableCell_();
            getSink().tableCell();
            getSink().text( String.valueOf( linkcheckFile.getNumberOfLinks() ) );
            getSink().tableCell_();
            getSink().tableCell();
            getSink().text( String.valueOf( linkcheckFile.getNumberOfLinks( LinkcheckFileResult.VALID_LEVEL ) ) );
            getSink().tableCell_();
            getSink().tableCell();
            getSink().text( String.valueOf( linkcheckFile.getNumberOfLinks( LinkcheckFileResult.WARNING_LEVEL ) ) );
            getSink().tableCell_();
            getSink().tableCell();
            getSink().text( String.valueOf( linkcheckFile.getNumberOfLinks( LinkcheckFileResult.ERROR_LEVEL ) ) );
            getSink().tableCell_();

            getSink().tableRow_();

            // Detail error
            if ( linkcheckFile.getUnsuccessful() != 0 )
            {
                getSink().tableRow();

                getSink().tableCell();
                getSink().text( "" );
                getSink().tableCell_();

                // TODO it is due to DOXIA-78
                getSink().rawText( "<td colspan=\"5\">" );

                getSink().table();

                for ( Iterator it2 = linkcheckFile.getResults().iterator(); it2.hasNext(); )
                {
                    LinkcheckFileResult linkcheckFileResult = (LinkcheckFileResult) it2.next();

                    if ( linkcheckFileResult.getStatusLevel() == LinkcheckFileResult.VALID_LEVEL )
                    {
                        continue;
                    }

                    getSink().tableRow();

                    getSink().tableCell();
                    if ( linkcheckFileResult.getStatusLevel() == LinkcheckFileResult.WARNING_LEVEL )
                    {
                        iconWarning( locale );
                    }
                    else if ( linkcheckFileResult.getStatusLevel() == LinkcheckFileResult.ERROR_LEVEL )
                    {
                        iconError( locale );
                    }
                    getSink().tableCell_();

                    getSink().tableCell();
                    getSink().italic();
                    if ( linkcheckFileResult.getTarget().startsWith( "#" ) )
                    {
                        getSink().link( linkcheckFile.getRelativePath() + linkcheckFileResult.getTarget() );
                    }
                    else if ( linkcheckFileResult.getTarget().startsWith( "." ) )
                    {
                        // We need to calculate a correct absolute path here, because target is a relative path
                        String absolutePath = FilenameUtils.getFullPath( linkcheckFile.getRelativePath() )
                            + linkcheckFileResult.getTarget();
                        String normalizedPath = FilenameUtils.normalize( absolutePath );
                        if ( normalizedPath == null )
                        {
                            normalizedPath = absolutePath;
                        }
                        getSink().link( normalizedPath );
                    }
                    else
                    {
                        getSink().link( linkcheckFileResult.getTarget() );
                    }
                    // Show the link as it was written to make it easy for
                    // the author to find it in the source document
                    getSink().text( linkcheckFileResult.getTarget() );
                    getSink().link_();
                    getSink().text( ": " );
                    getSink().text( linkcheckFileResult.getErrorMessage() );
                    getSink().italic_();
                    getSink().tableCell_();

                    getSink().tableRow_();
                }

                getSink().table_();

                getSink().tableCell_();

                getSink().tableRow_();
            }
        }

        getSink().table_();

        getSink().section1_();
    }

    private void generateTableHeader( Locale locale, boolean detail )
    {
        getSink().tableRow();
        if ( detail )
        {
            getSink().rawText( "<th rowspan=\"2\">" );
            getSink().text( "" );
            getSink().tableHeaderCell_();
        }
        getSink().rawText( "<th rowspan=\"2\">" );
        getSink().text(
                        detail ? i18n.getString( "sitegen-linkcheck-report", locale,
                                                 "report.sitegen.linkcheck.detail.table.documents" )
                                        : i18n.getString( "sitegen-linkcheck-report", locale,
                                                          "report.sitegen.linkcheck.summary.table.documents" ) );
        getSink().tableHeaderCell_();
        // TODO it is due to DOXIA-78
        getSink().rawText( "<th colspan=\"4\" align=\"center\">" );
        getSink().text( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.table.links" ) );
        getSink().tableHeaderCell_();
        getSink().tableRow_();

        getSink().tableRow();
        getSink().tableHeaderCell();
        getSink().text( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.table.totalLinks" ) );
        getSink().tableHeaderCell_();
        getSink().tableHeaderCell();
        iconValid( locale );
        getSink().tableHeaderCell_();
        getSink().tableHeaderCell();
        iconWarning( locale );
        getSink().tableHeaderCell_();
        getSink().tableHeaderCell();
        iconError( locale );
        getSink().tableHeaderCell_();
        getSink().tableRow_();
    }

    private void iconError( Locale locale )
    {
        getSink().figure();
        getSink().figureCaption();
        getSink().text( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.icon.error" ) );
        getSink().figureCaption_();
        // should be defined in skins
        getSink().figureGraphics( "images/icon_error_sml.gif" );
        getSink().figure_();
    }

    private void iconValid( Locale locale )
    {
        getSink().figure();
        getSink().figureCaption();
        getSink().text( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.icon.valid" ) );
        getSink().figureCaption_();
        // should be defined in skins
        getSink().figureGraphics( "images/icon_success_sml.gif" );
        getSink().figure_();
    }

    private void iconWarning( Locale locale )
    {
        getSink().figure();
        getSink().figureCaption();
        getSink().text( i18n.getString( "sitegen-linkcheck-report", locale, "report.sitegen.linkcheck.icon.warning" ) );
        getSink().figureCaption_();
        // should be defined in skins
        getSink().figureGraphics( "images/icon_warning_sml.gif" );
        getSink().figure_();
    }

    // ----------------------------------------------------------------------
    // static methods
    // ----------------------------------------------------------------------

    /**
     * Similar to {@link Arrays#toString(int[])} in 1.5.
     *
     * @param a not null
     * @return the array comma separated.
     */
    private static String toString( int[] a )
    {
        if ( a == null || a.length == 0 )
        {
            return "";
        }

        StringBuffer buf = new StringBuffer();
        buf.append( a[0] );

        for ( int i = 1; i < a.length; i++ )
        {
            buf.append( ", " );
            buf.append( a[i] );
        }

        return buf.toString();
    }
}
