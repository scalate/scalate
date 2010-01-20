import com.mh.serverpages.util.XmlEscape
import javax.servlet.http._

class _sample_ssp extends HttpServlet {
  override def service( request: HttpServletRequest, response: HttpServletResponse ): Unit = {
    val out = new java.io.PrintWriter( new java.io.OutputStreamWriter( response.getOutputStream, "UTF-8" ) )

    out.write( "<html>\u000a" )

    
  import com.mh.serverpages.sample.Snippets._


    out.write( "\u000a<body>\u000a" )

    out.write( _safeToString(  cheese  ) )

    out.write( "\u000a</body>\u000a</html>" )

    out.close
  }

  private def _safeToString( x: Any ): String = x match {
    case r: AnyRef => if( r == null ) "null" else r.toString
    case v: AnyVal => v.toString
    case _ => "null" 
  }
  
}