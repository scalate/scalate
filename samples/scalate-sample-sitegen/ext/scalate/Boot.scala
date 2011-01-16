package scalate

import org.fusesource.scalate.util.Logging
import java.util.concurrent.atomic.AtomicBoolean

class Boot extends Logging {

  private var _initialised = new AtomicBoolean(false)

  def run: Unit = {
    if (_initialised.compareAndSet(false, true)) {
      println(">>>> Scalate SiteGen bootstrap invoked!!")
      info(">>>> Scalate SiteGen bootstrap invoked!!")
    }
  }
}