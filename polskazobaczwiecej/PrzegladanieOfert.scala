package polskazobaczwiecej

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.util.Random

class PrzegladanieOfert extends Simulation {

  val httpConf  = http
    .baseUrl("https://polskazobaczwiecej.pl")
    .doNotTrackHeader("1")
  	.acceptEncodingHeader("gzip, deflate")

  def randomRegion() = Random.nextInt(15) + 1

  val scn = scenario("Sprawdz oferty")
  	.exec(http("Strona glowna").get("/").check(status.not(404)))
  	.pause(1, 10)
  	.exec(http("Informacje").get("/informacje").check(status.not(404)))
  	.pause(1, 10)
  	.exec(http("Regulamin").get("/REGULAMIN-wiosna-2019.pdf").check(status.not(404)))
  	.pause(1, 20)
  	.exec(http("Sprawdź oferty").get("/lista-obiektow").check(status.not(404)))
  	.pause(3, 15)
  	.exec(http("Filtruj").get("/lista-obiektow?s=&r={randomRegion()}&m=").check(css("div.ob-list h3 > a", "href").saveAs("oferta")))
  	.pause(3, 10)
  	.exec(http("Szczegoly oferty").get("${oferta}"))
  	.pause(3, 15)

  setUp(scn.inject(atOnceUsers(1)).protocols(httpConf))
}
