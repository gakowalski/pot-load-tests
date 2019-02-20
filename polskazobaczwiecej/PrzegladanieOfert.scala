package polskazobaczwiecej

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import java.util.concurrent.ThreadLocalRandom

class PrzegladanieOfert extends Simulation {

  val httpConf  = http
    .baseUrl("https://polskazobaczwiecej.pl")
    .doNotTrackHeader("1")
  	.acceptEncodingHeader("gzip, deflate")

  def randomRegion() = ThreadLocalRandom.current.nextInt(15) + 1

  val czytajRegulamin = exec(http("Regulamin").get("/REGULAMIN-wiosna-2019.pdf").check(status.not(404))).pause(1, 20)
  var czytajInformacje = exec(http("Informacje").get("/informacje").check(status.not(404))).pause(1, 10)

  val uwaznyInternauta = randomSwitch(
    	10d -> exec(czytajRegulamin).exec(czytajInformacje),
    	90d -> exec(czytajInformacje)
    )

  val sprawdzOferty = exec(http("Sprawdź oferty").get("/lista-obiektow").check(status.not(404)))
  	.pause(15, 30)
  	.exec(http("Sprawdz oferty").get("/lista-obiektow/strona-2").check(status.not(404)))
  	.pause(15, 30)
  	.exec(http("Filtruj").get("/lista-obiektow?s=&r={randomRegion()}&m=").check(css("div.ob-list h3 > a", "href").saveAs("oferta")))
  	.pause(3, 10)
  	.exec(http("Szczegoly oferty").get("${oferta}"))
  	.pause(15, 45)

  val scn = scenario("Sprawdz oferty")
  	.exec(http("Strona glowna").get("/").check(status.not(404)))
  	.pause(1, 10)
  	.randomSwitch(
    	50d -> exec(uwaznyInternauta).exec(sprawdzOferty),
  		50d -> exec(sprawdzOferty)
    )


  setUp(scn.inject(atOnceUsers(10)).protocols(httpConf))
}
