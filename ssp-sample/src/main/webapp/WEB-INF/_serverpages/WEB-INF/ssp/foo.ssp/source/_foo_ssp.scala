package _WEB_INF._ssp

import com.mh.serverpages.util.XmlEscape
import javax.servlet.http._

class _foo_ssp extends HttpServlet {
  override def service( request: HttpServletRequest, response: HttpServletResponse ): Unit = {
    val out = new java.io.PrintWriter( new java.io.OutputStreamWriter( response.getOutputStream, "UTF-8" ) )

    

    
  import com.mh.serverpages.sample._
  import java.util.Date

  val foo = request.getAttribute( "foo" ).asInstanceOf[Foo]
  val timestamp = request.getAttribute( "timestamp" ).asInstanceOf[Date]


    out.write( "\u000a\u000a" )

    

    out.write( "\u000a\u000a<h1>Test Scala Server Page</h1>\u000a\u000a<p>\u000a  This test page displays data passed from a servlet.  Browsers can also <a href=\"scala/standalone.ssp\">request SSPs directly</a>.\u000a</p>\u000a\u000a<p>\u000a  The current time is: " )

    out.write( XmlEscape.escape( _safeToString( timestamp ) ) )

    out.write( "\u000a</p>\u000a\u000a<p>\u000a  Your HTTP request included the following headers:\u000a  <ul>\u000a    " )

     foo.requestHeaders.foreach { header => 

    out.write( "\u000a      <li>" )

    out.write( XmlEscape.escape( _safeToString( header ) ) )

    out.write( "</li>\u000a    " )

     } 

    out.write( "\u000a  </ul>\u000a</p>\u000a\u000a<p>\u000a  The pathinfo was: " )

    out.write( XmlEscape.escape( _safeToString( foo.pathInfo ) ) )

    out.write( "\u000a</p>\u000a" )

    out.close
  }

  private def _safeToString( x: Any ): String = x match {
    case r: AnyRef => if( r == null ) "null" else r.toString
    case v: AnyVal => v.toString
    case _ => "null" 
  }
  
}