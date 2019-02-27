package polskazobaczwiecej

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class PrzegladanieOfert extends Simulation {

  val httpConf  = http
    .baseUrl("https://polskazobaczwiecej.pl")
    .doNotTrackHeader("1")
  	.acceptEncodingHeader("gzip, deflate")
    .inferHtmlResources(BlackList(
      "https://www.google.com/.*",
      "https://fonts.googleapis.com/.*",
      "https://www.googletagmanager.com/.*"
    ))
    .enableHttp2

  val czytajRegulamin = exec(http("Regulamin").get("/REGULAMIN-wiosna-2019.pdf").check(status.not(404))).pause(1, 20)
  val czytajInformacje = exec(http("Informacje").get("/informacje").check(status.not(404))).pause(1, 10)

  val uwaznyInternauta = randomSwitch(
    	10d -> exec(czytajRegulamin).exec(czytajInformacje),
    	90d -> exec(czytajInformacje)
    )

  val sprawdzOferty = exec(
      http("Sprawdz oferty (str. 1)").get("/lista-obiektow")
      .check(
        status.not(404),
        css("option+option", "value").findAll.saveAs("wojewodztwa"),
        css("input[name='c[]']", "value").findAll.saveAs("uslugi")
      )
    )
  	.pause(15, 30)
    .doIfOrElse("${wojewodztwa.exists()}") {
      exec(http("Sprawdz oferty (str. 2)").get("/lista-obiektow/strona-2").check(
        status.not(404)
      ))
    } {
      exec(http("Sprawdz oferty (str. 2)").get("/lista-obiektow/strona-2").check(
        status.not(404),
        css("option+option", "value").findAll.saveAs("wojewodztwa")
      ))
    }
  	.pause(15, 30)
    .doIf("${wojewodztwa.exists()}") {
      doIfOrElse("${uslugi.exists()}") {
        exec(http("Filtruj (woj.)").get("/lista-obiektow?s=&r=${wojewodztwa.random()}&m=")
          .check(css("div.ob-list h3 > a", "href").findAll.saveAs("oferta_1"))
        )
      } {
        exec(http("Filtruj (woj.)").get("/lista-obiektow?s=&r=${wojewodztwa.random()}&m=")
          .check(
            css("div.ob-list h3 > a", "href").findAll.saveAs("oferta_1"),
            css("input[name='c[]']", "value").findAll.saveAs("uslugi")
          )
        )
      }
      .pause(5, 15)
    }
    .doIf("${oferta_1.exists()}") {
      exec(http("Szczegoly oferty #1").get("${oferta_1.random()}"))
      .pause(15, 45)
    }
    .doIf("${wojewodztwa.exists()}") {
      doIf("${uslugi.exists()}") {
        exec(http("Filtruj (woj. + usluga)").get("/lista-obiektow")
          .queryParam("s", "")
          .queryParam("r", "${wojewodztwa.random()}")
          .queryParam("m", "")
          .queryParam("c[]", "${uslugi.random()}")
          .check(css("div.ob-list h3 > a", "href")
          .findAll
          .optional
          .saveAs("oferty"))
        )
      }
    }
  	.pause(5, 15)
    .doIf("${oferty.exists()}") {
      exec(http("Szczegoly oferty #2").get("${oferty.random()}"))
    	.pause(15, 45)
    }


  val scn = scenario("Przegladanie Ofert")
  	.exec(http("Strona glowna").get("/").check(status.not(404)))
  	.pause(1, 10)
  	.randomSwitch(
    	50d -> exec(uwaznyInternauta).exec(sprawdzOferty),
  		50d -> exec(sprawdzOferty)
    )

  val nbUsers = Integer.getInteger("users", 10)

  setUp(scn.inject(atOnceUsers(nbUsers)).protocols(httpConf))
}
