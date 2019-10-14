package pzw_laravel

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.http.response._
import java.nio.charset.StandardCharsets.UTF_8
import scala.Predef._

class PrzegladanieOfert extends Simulation {

  val httpConf  = http
    .baseUrl("https://deploy.polskazobaczwiecej.pl")
    .doNotTrackHeader("1")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("pl;q=0.9,en;q=0.8")
    .inferHtmlResources(BlackList(
      "https://www.google.com/.*",
      "https://fonts.googleapis.com/.*",
      "https://www.googletagmanager.com/.*",
      "https://unpkg.com/.*",
      "https://stackpath.bootstrapcdn.com/.*",
      "https://code.jquery.com/.*",
      "https://cdnjs.cloudflare.com/.*",
      "https://maps.googleapis.com/.*"
    ))
    .enableHttp2

  val czytajRegulamin = exec(http("Regulamin").get("/Regulamin%20POLSKA%20ZOBACZ%20WI%C4%98CEJ%20-%20WEEKEND%20ZA%20P%C3%93%C5%81%20CENY%20jesie%C5%84%202019.pdf").check(status.not(404))).pause(1, 20)
  val czytajInformacje = exec(http("Informacje").get("/informacje").check(status.not(404))).pause(1, 10)

  val uwaznyInternauta = randomSwitch(
    	10d -> exec(czytajRegulamin).exec(czytajInformacje),
    	90d -> exec(czytajInformacje)
    )

  val sprawdzOferty = exec(
      http("Sprawdz oferty (pierwsza strona)").get("/przegladanie-ofert")
      .check(
        status.not(404),
        css("option+option", "value").findAll.saveAs("wojewodztwa"),
        css("input[name='character[]']", "value").findAll.saveAs("uslugi"),
	css("input[name='_token']", "value").find.saveAs("token"),
	css("a.page-link", "href").findAll.saveAs("pages"),
      )
    )
    .pause(15, 30)
    .doIf("${pages.exists()}") {
      exec(
        http("Sprawdz oferty (losowa strona)").get("${pages.random()}&_token=${token}&offer_title=&region=&city=")
        .check(
	  status.is(200),
        )
      )
      .pause(10, 20)
    }
    .doIf("${wojewodztwa.exists()}") {
      exec(http("Filtruj (woj.)").get("/przegladanie-ofert/fetch_data?page=1&_token=${token}&offer_title=&region=${wojewodztwa.random()}&city=")
        .check(
	  status.is(200),
        )
      )
      .pause(10, 20)
    }
    .doIf("${uslugi.exists()}") {
        exec(http("Filtruj (usluga)").get("/przegladanie-ofert/fetch_data?page=1&_token=${token}&offer_title=&region=&city=&character%5B%5D=${uslugi.random()}")
          .check(
            status.is(200),
          )
	)
	.exec(http("Filtruj markery (uslugi)").get("/przegladanie-ofert/fetch_markers?_token=${token}&offer_title=&region=&city=&character%5B%5D=${uslugi.random()}")
	  .check(
	    status.is(200)
	  )
	)
        .pause(10, 20)
    }
    .doIf("${wojewodztwa.exists()}") {
      doIf("${uslugi.exists()}") {
        exec(http("Filtruj (woj. + usluga)").get("/przegladanie-ofert/fetch_data?page=1&_token=${token}&offer_title=&region=${wojewodztwa.random()}&city=&character%5B%5D=${uslugi.random()}")
	  .check(
	    status.is(200),
	  )
	)
	.exec(http("Filtruj markery (woj. + usluga)").get("/przegladanie-ofert/fetch_markers?_token=${token}&offer_title=&region=${wojewodztwa.random()}&city=&character%5B%5D=${uslugi.random()}")
	  .check(
            status.is(200)
          )
	)
	.pause(10, 20)
      }
    }

  val scn = scenario("Przegladanie Ofert")
  	.exec(http("Strona glowna").get("/").check(status.not(404)))
  	.pause(1, 10)
  	.randomSwitch(
    	50d -> exec(uwaznyInternauta).exec(sprawdzOferty),
  	50d -> exec(sprawdzOferty)
    )

  val nbUsers = Integer.getInteger("users", 1000)

  setUp(scn.inject(atOnceUsers(nbUsers)).protocols(httpConf))
}
