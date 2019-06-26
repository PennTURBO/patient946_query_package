package example

import org.scalatest.FunSuite
import scala.io.Source
import com.typesafe.config.ConfigFactory

class upAndRunning extends FunSuite {

  var defaultConfig = ConfigFactory.parseResources("defaults.conf")
  var dcCn = defaultConfig.getString("conf.moreRelevant")
  println(dcCn)

  test("1 plus 2 equals 3") {
    val x = 1 + 2
    assert(x == 3)
  }

}