package scalate

import java.util.concurrent.atomic.AtomicBoolean
import org.fusesource.scalate.util.Log

object Boot extends Log
class Boot {
  import Boot._

  private var _initialised = new AtomicBoolean(false)

  def run: Unit = {
    if (_initialised.compareAndSet(false, true)) {
      println(">>>> Scalate SiteGen bootstrap invoked!!")
      info(">>>> Scalate SiteGen bootstrap invoked!!")
    }
  }
}