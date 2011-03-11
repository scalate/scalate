<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

<title>Content Based Routing</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<meta name="copyright" content="Copyright (c) 2007-2009, Progress Software Corporation" />
<meta name="keywords" content="content-based routing, SOA">
<meta name="description" content="FuseSource - open source SOA software for content-based routing.">
<link rel="shortcut icon" href="http://fusesource.com/favicon.ico" />


<link href="http://tools.microformatic.com/transcode/rss/hatom/http://fusesource.com" rel="alternate" title="News and Events" type="application/rss+xml" />
<link href="http://fusesource.com/blogs" rel="alternate" title="What's the FUSE Team Saying" type="application/rss+xml" />

<style type="text/css">
.tabber {margin-top: 30px;}
.tabberlive {margin-left: 0;}
iframe { display: block;}
</style>

<link rel="stylesheet" href="/styles/lightbox.css" media="screen,projection" type="text/css" />
<link rel="stylesheet" type="text/css" href="/css/forms.css" />
<link rel="stylesheet" href="/css/main.css" type="text/css" />
<link rel="stylesheet" href="/css/tables.css" type="text/css" />
<link rel="stylesheet" href="/styles/example.css" type="text/css" media="screen" />
<script type="text/javascript">document.write('<style type="text/css">.tabber{display:none;}<\/style>');</script>
<link rel="stylesheet" href="/css/fusesource.css" type="text/css" />

<script type="text/javascript" src="/scripts/tabber.js"></script>
<script type="text/javascript" src="/javascripts/prototype.js" ></script>
<script type="text/javascript" src="/scripts/lightbox.js"></script>
<script type="text/javascript" src="http://www.landingpg.com/lp-tracking/lp.js"></script>

<script type="text/javascript">

/* Play a video in a popup window */

function PopUpMiniPlayer(url) {

    var playerWindow;
    playerWindow = window.open(url,"win",'toolbar=0,location=0,directories=0,status=1,menubar=0,scrollbars=0,resizable=0,width=880,height=680');
    playerWindow.focus();
}
</script>
</head>



  <body id="">


<script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write("\<script src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'>\<\/script>" );
</script>
<script type="text/javascript">
var pageTracker = _gat._getTracker("UA-586989-2");
pageTracker._initData();
pageTracker._trackPageview();
</script>


<div id="centered">
  <div id="border">
    <div id="header">
        <div id="header_logo">
            <a href="/"><img src="/images/header_logo.png" /></a>
        </div>
    	<div id="navigation">
    	<ul class="level1">
          <li class="submenu first" id="nav-products"><a href="/products/" class="first">Products</a>
            <ul class="level2">
              <li><a href="/products/enterprise-servicemix/"><span class="nav_heading">Fuse ESB</span><br />
              based on Apache ServiceMix</a></li>
              <li><a href="/products/enterprise-camel/"><span class="nav_heading">Fuse Mediation Router</span><br />
              based on Apache Camel</a></li>
              <li><a href="/products/enterprise-activemq/"><span class="nav_heading">Fuse Message Broker</span><br />
              based on Apache ActiveMQ</a></li>
              <li><a href="/products/enterprise-cxf/"><span class="nav_heading">Fuse Services Framework</span><br />
              based on Apache CXF</a></li>
              <li><a href="/products/fuse-hq/"><span class="nav_heading">Fuse HQ</span><br />
              manage and monitor FuseSource infrastructure</a></li>
            </ul>
          </li>
          <li id="nav-downloads"><a href="/downloads/" class="middle">Downloads</a></li>
          <li class="submenu" id="nav-support"><a href="/enterprise-support/" class="middle">Services</a>
            <ul class="level2">
              <li><a href="/enterprise-support/fusesource-support/"><span class="nav_heading">Technical Support</span><br />
              FuseSource support and maintenance services</a></li>	
              <li><a href="/enterprise-support/getting-started/"><span class="nav_heading">Getting Started</span><br />
              free tutorials and demos for ServiceMix, Camel, ActiveMQ and CXF</a></li>		
              <li><a href="/enterprise-support/pilot-support/"><span class="nav_heading">Pilot Subscriptions</span><br />
              ensure a successful pilot and maximize technical benefit</a></li>
              <li><a href="/enterprise-support/support-offerings/"><span class="nav_heading">Enterprise Subscriptions</span><br />
              support for ServiceMix, Camel, ActiveMQ and CXF</a></li>
              <li><a href="/enterprise-support/consulting/"><span class="nav_heading">Consulting</span><br />
              consulting for ServiceMix, Camel, ActiveMQ and CXF</a></li>			
              <li><a href="/enterprise-support/virtual-training/"><span class="nav_heading">Virtual Training</span><br />
              interactive, web-based training courses for ServiceMix, Camel, ActiveMQ and CXF</a></li>
              <li><a href="/enterprise-support/on-site-training/"><span class="nav_heading">On-Site Training</span><br />
    		  personalized training for ServiceMix, Camel, ActiveMQ and CXF at your facility</a></li>
            </ul>
          </li>
          <li class="submenu" id="nav-community"><a href="/community/" class="middle">Community</a>
            <ul class="level2">
              <li><a href="/community/fuse-customers"><span class="nav_heading">FuseSource Customers</span><br />
              FuseSource success stories</a></li>
              <li><a href="/forge"><span class="nav_heading">FuseSource Forge</span><br />
              develop open source projects</a></li>
              <li><a href="/forums"><span class="nav_heading">Forums</span><br />
              have questions? have answers? please visit our forums</a></li>
              <li><a href="/resources/fuse-open-source-blogs/"><span class="nav_heading">Blogs</span><br />
              tune in to hear what the FuseSource team is saying</a></li>
              <li><a href="/community/apache-committers-and-fuse/"><span class="nav_heading">Apache Committers</span><br />
              Our involvement at Apache</a></li>
              <li><a href="/issues/secure/Dashboard.jspa"><span class="nav_heading">Issue Tracker</span><br />
              check enhancement requests</a></li>
              <li><a href="/wiki"><span class="nav_heading">Wiki</span><br />
              collaborative documentation on the FuseSource line of products</a></li>
            </ul>
          </li>
          <li class="submenu" id="nav-documentation"><a href="/documentation/" class="middle">Documentation</a>
            <ul class="level2">
              <li><a href="/products/enterprise-servicemix/#documentation"><span class="nav_heading">Fuse ESB</span><br />
              based on Apache ServiceMix</a></li>
              <li><a href="/products/enterprise-camel/#documentation"><span class="nav_heading">Fuse Mediation Router</span><br />
              based on Apache Camel</a></li>
              <li><a href="/products/enterprise-activemq/#documentation"><span class="nav_heading">Fuse Message Broker</span><br />
              based on Apache ActiveMQ</a></li>
              <li><a href="/products/enterprise-cxf/#documentation"><span class="nav_heading">Fuse Services Framework</span><br />
              based on Apache CXF</a></li>
              <li><a href="/documentation/fuse-hq-documentation"><span class="nav_heading">Fuse HQ</span><br />
              manage and monitor FuseSource infrastructure</a></li>
            </ul>
          </li>
          <li class="submenu" id="nav-resources"><a href="/resources/" class="middle">Resources</a>
            <ul class="level2">
              <li><a href="/fuse/apache-books/"><span class="nav_heading">Hot New Apache Books!</span><br />
              our rock stars can write</a></li>
              <li><a href="/resources/video-archived-webinars/"><span class="nav_heading">Webinars</span><br />
              expert instruction</a></li>
              <li><a href="/resources/fuse-tv/"><span class="nav_heading">FuseSource TV</span><br />
              meet leaders at Apache</a></li>
              <li><a href="/resources/analyst-reports/"><span class="nav_heading">Analyst Reports</span><br />
              industry papers and reports</a></li>
              <li><a href="/resources/collateral/"><span class="nav_heading">Collateral</span><br />
              data sheets and white papers</a></li>
              <li><a href="/resources/faqs/"><span class="nav_heading">FAQs</span><br />
              open source and product Q&amp;A</a></li>
              <li><a href="/resources/podcasts/"><span class="nav_heading">Podcasts</span><br />
              discussions on open source SOA</a></li>
            </ul>
          </li>
          <li class="submenu last" id="nav-about"><a href="/about-this-site/" class="last">About us</a>
            <ul class="level2">
              <li><a href="/community/events/"><span class="nav_heading">Events</span><br />
              upcoming activities</a></li>
              <li><a href="/press-releases"><span class="nav_heading">Press releases</span><br />
              the latest news</a></li>
              <li><a href="/about-this-site/management/"><span class="nav_heading">Management</span><br />
              a team of open source experts</a></li>
              <li><a href="/resources/in-the-news/"><span class="nav_heading">In the News</span><br />
              in their words</a></li>
              <li><a href="/community/why"><span class="nav_heading">Why FuseSource</span><br />
              FuseSource and Apache</a></li>
              <li><a href="/partners"><span class="nav_heading">Partners</span><br />
              more members of our community</a></li>
              <li><a href="/partners/become-a-fuse-partner"><span class="nav_heading">Become a FuseSource partner</span><br />
              join our community</a></li>
              <li><a href="/about-this-site/"><span class="nav_heading">About this site</span><br />
              FuseSource community Web site</a></li>
              <li><a href="/contact"><span class="nav_heading">Contact us</span><br />
              we would love to hear from you</a></li>
            </ul>
          </li>
        </ul>
	</div>
    </div>
    
    <div id="main">
    	<div id="main_bg_bottom">
    	        <ul class="actions">
            <li id="loggedOut"><a href="/login?clicked=yes">Login</a> |</li>
            <li id="loggedIn" style="display:none"><span id="greeting">Greeting</span>, <a href="/logout">Log out</a> |</li>
            <li><a href="/register">Register</a> |</li>
            <li><a href="http://form.fusesource.com/forms/getsupportnow">Buy Now</a> |</li>
            <li><a href="/downloads/">Download</a></li>
    	</ul>
        
        <div class="col_left">
        	<div class="subpage">
            	<h1>Content Based Routing</h1>
                <h2>Content-based routing enhances <span class="caps">SOA</span>.</h2>


	<p>In service-oriented architecture (SOA), routing messages based on content is a significant functionality that helps make <span class="caps">SOA</span> more flexible. Content-based routing routes messages based on the actual content of the message itself, rather than by a destination specified by the message. Content-based routing works by opening a message and applying a set of rules to its content to determine the message’s destination. By freeing the sending application from needing to know anything about where a message is headed, content-based routing provides a high degree of flexibility and adaptability to change ― essentials of a successful <span class="caps">SOA</span>.</p>


	<p>For organizations seeking an <span class="caps">SOA</span> infrastructure that accommodates content-based routing, FuseSource offers leading open source solutions.</p>


	<h3>Learn more about <a href="/products/enterprise-camel">FuseSource and content-based routing</a> now.</h3>


	<h2><a href="/products/enterprise-camel"><span class="caps">FUSE</span> Mediation Router</a> delivers content-based routing functionality.</h2>


	<p><span class="caps">FUSE</span> Mediation Router is enterprise, open source <span class="caps">SOA</span> software based on Apache Camel for content-based routing. <span class="caps">FUSE</span> Mediation Router is tested, certified and fully supported by the FuseSource team. This powerful rule-based routing and process mediation engine offers the ease of basic <span class="caps">POJO</span> development with the clarity of the standard Enterprise Integration Patterns (EIP). <span class="caps">FUSE</span> Mediation Router can be deployed inside any container or as a stand-alone, and can work directly with any kind of transport or messaging model to quickly integrate existing services and applications. <span class="caps">FUSE</span> Mediation Router provides greater ease of use by relying a Java Domain Specific Language as well as Spring <span class="caps">XML</span> for configuring routing rules, including content-based rules. This gives developers the ability to create powerful and concise code and to support type-safe smart completion, reducing the need to work with great numbers of <span class="caps">XML</span> configuration files.</p>


	<h3>Learn more about <a href="/products/enterprise-camel"><span class="caps">FUSE</span> Mediation Router and content-based routing</a> now.</h3>


	<p>The FuseSource team also offers an <a href="/open_source/esb"><span class="caps">ESB</span></a>, <a href="/open_source/jbi"><span class="caps">JBI</span></a> technology, <a href="/open_source/open-source-messaging">open source messaging</a>, <a href="/open_source/restful-services">RESTful Services</a> and <a href="/open_source/soa"><span class="caps">SOA</span></a> tools.</p>
        	</div>
        </div>
        
        <div id="col_right">
        	      

		      
		        <div class="col_right_callout col_right_callout_first">
		          <h2>Download Free Today</h2>

    <p>Unix, Windows and source:</p>

    <ul>
    <li><a href="/products/enterprise-servicemix/#download">Fuse <span class="caps"><span class="caps">ESB</span></span></a> </li>
        <li><a href="/products/enterprise-camel/#download">Fuse Mediation Router</a></li>
        <li><a href="/products/enterprise-activemq/#download">Fuse Message Broker</a> </li>
        <li><a href="/products/enterprise-cxf/#download">Fuse Services Framework</a></li>
    </ul>
		        </div>
		      
                      
                      
                              

		      
		      
		        <div class="col_right_callout">
		            <h2>Getting Started?</h2>

    <p>Help when you need it:</p>

    <ul>
    <li><a href="/resources/collateral">Datasheets</a> &#8211; all products </li>
        <li><a href="/forums">Forums</a> &#8211; active Q &#38; A</li>
        <li><a href="/wiki">Wiki</a> &#8211; wealth of info</li>
        <li><a href="/enterprise-support/support-offerings">Support</a> &#8211; enterprise-class</li>
        <li><a href="/resources/video-archived-webinars">Videos</a> &#8211; training</li>
        <li><a href="/resources/podcasts">Podcasts</a> &#8211; tips and tricks</li>
        <li><a href="/resources/collateral/#whitepapers">Papers</a> &#8211; guides</li>
    </ul>
		        </div>
		      
		      
		      
                     
                      <div id="sidebar_logos" class="sidebar">
                         <div align="center" class="logos">
<img src="/images/osslo2.PNG" alt="Open Source Software Suppliers Organisation" style="margin-bottom:20px;" />
</div>
                      </div>
   
        </div>
<div id="social_icon_set">
          <div class="social_icon">
            <a href="http://youtube.com/fusesource" target="_blank"><img src="/images/icon_youtube.jpg" alt="YouTube" /></a>
          </div>
          <div class="social_icon">
            <a href="http://twitter.com/fusenews" target="_blank"><img src="/images/icon_twitter.jpg" alt="Twitter" /></a>
          </div>
          <div class="social_icon">
            <a href="http://www.linkedin.com/groups?mostPopular=&gid=2117744" target="_blank"><img src="/images/icon_linkedin.jpg" alt="Linkedin" /></a>
          </div>
          <div class="social_icon">
            <a href="http://www.facebook.com/pages/FuseSource/154734551230506?v=wall" target="_blank"><img src="/images/icon_facebook.jpg" alt="FaceBook" /></a>
          </div>
          <div class="social_icon">
            <a href="http://www.flickr.com/photos/54898272@N07/"><img src="/images/icon_flickr.jpg" alt="Flickr" /></a>
          </div>
          <div class="social_icon" style="width:49px;">
            <iframe src="http://www.facebook.com/plugins/like.php?href=http%3A%2F%2Ffusesource.com&amp;layout=button_count&amp;show_faces=true&amp;width=49&amp;action=like&amp;colorscheme=light&amp;height=21" scrolling="no" frameborder="0" style="border:none; overflow:hidden; width:49px; height:21px;" allowTransparency="true"></iframe>
          </div>
        </div>
        </div>
    <div id="footer_text">
          <div class="wrapper">
<p style="text-align: center; font-size:80%;">
<a href="/contact">Contact Us</a> | <a href="/about-this-site/">About This Site</a> | <a href="/about-this-site/legal-terms-and-privacy-policy">Legal Terms and Privacy Policy</a> | <a href="/about-this-site/site-index/">Site Index</a> </p> 
		<p style="text-align: center; font-size:80%;" id="copyright"> &copy; 2006-2011 <a href="http://www.progress.com">Progress Software Corporation</a> and its subsidiaries or affiliates.  All rights reserved. </p>

<div class="about_footer">
<p style="color:#666666;"><a href="/" style="color:#666666;">FuseSource Open Source Community</a> is a community resource for open source <a href="/open_source/service-oriented-architecture" style="color:#666666;">service oriented architecture</a> <a href="/open_source/soa/" style="color:#666666;">(SOA)</a> and <a href="/open_source/application-integration/" style="color:#666666;">application integration</a> products and technologies. FuseSource offers products based on <a href="http://fusesource.com/products/enterprise-servicemix/" style="color:#666666;">Apache ServiceMix</a>, <a href="http://fusesource.com/products/enterprise-activemq/" style="color:#666666;">Apache ActiveMQ</a>, <a href="http://fusesource.com/products/enterprise-cxf/" style="color:#666666;">Apache CXF</a> and <a href="http://fusesource.com/products/enterprise-camel/" style="color:#666666;">Apache Camel</a> which are certified, productized and fully supported by the <a href="http://fusesource.com/community/apache-committers-and-fuse" style="color:#666666;">people who wrote the code</a>. Learn about ActiveMQ with our training, support, and consulting on <a href="/open_source/activemq-performance/" style="color:#666666;">ActiveMQ performance</a>. FuseSource also offers <a href="/open_source/integration-software/" style="color:#666666;">integration software</a> that allows developers to service-enable existing components or build new services with preconfigured <a href="/open_source/enterprise-integration-patterns/" style="color:#666666;">enterprise integration patterns</a>. FuseSource <a href="/open_source/esb/" style="color:#666666;">ESB</a> 4 for <a href="/open_source/enterprise-service-bus/" style="color:#666666;">enterprise service bus</a> is an open source integration platform for BPEL components that supports the latest emerging standards like <a href="/products/enterprise-servicemix4" style="color:#666666;">OSGi</a> and <a href="/open_source/jbi/" style="color:#666666;">JBI</a> 2.0. Fuse Services Framework allows Java developers to create reusable <a href="/open_source/restful-services/" style="color:#666666;">RESTful services</a> using front-end programming APIs like <a href="/open_source/jax-ws/" style="color:#666666;">JAX-WS</a>. Our powerful <a href="/open_source/message-oriented-middleware/" style="color:#666666;">message oriented middleware</a> solution, Fuse Message Broker, offers a cost-effective and flexible <a href="/open_source/open-source-messaging/" style="color:#666666;">open source messaging</a> platform. FuseSource  also works to enhance your SOA using <a href="/open_source/content-based-routing/" style="color:#666666;">content based routing</a> with Fuse Mediation Router.</p>
</div>

                <!--[if !IE]><hr><![endif]-->
	</div>
     
      
        </div>
    </div>
    <div id="footer"></div>
  </div>
</div>

<script type="text/javascript" src="/javascripts/prototype_cookies.js"></script>
<script type="text/javascript" src="/javascripts/autologin.js" ></script>
<script type="text/javascript" src="/javascripts/application.js" ></script>

<SCRIPT TYPE='text/javascript' LANGUAGE='JavaScript' SRC='/elqNow/elqCfg.js'></SCRIPT>
<SCRIPT TYPE='text/javascript' LANGUAGE='JavaScript' SRC='/elqNow/elqImg.js'></SCRIPT>
<script type='text/javascript' language='JavaScript'><!-- 
  var elqPPS = '70';
//-->
</script>
<script type='text/javascript' language='JavaScript' src="/elqNow/elqScr.js"></script>



</body>
</html>
