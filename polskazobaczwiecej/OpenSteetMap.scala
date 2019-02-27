package polskazobaczwiecej

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class OpenStreetMap extends Simulation {

  val httpConf  = http
    .baseUrl("https://maps.pot.gov.pl/tile")
  	.acceptEncodingHeader("gzip, deflate")

  val mapa_Polska =
    exec(http("/6/35/20.png").get("/6/35/20.png").check(status.not(404)))
    .exec(http("/6/35/21.png").get("/6/35/21.png").check(status.not(404)))
    .exec(http("/6/34/20.png").get("/6/34/20.png").check(status.not(404)))
    .exec(http("/6/36/20.png").get("/6/36/20.png").check(status.not(404)))
    .exec(http("/6/34/21.png").get("/6/34/21.png").check(status.not(404)))
    .exec(http("/6/36/21.png").get("/6/36/21.png").check(status.not(404)))
    .exec(http("/6/33/20.png").get("/6/33/20.png").check(status.not(404)))
    .exec(http("/6/37/20.png").get("/6/37/20.png").check(status.not(404)))
    .exec(http("/6/37/21.png").get("/6/37/21.png").check(status.not(404)))

  val scn = scenario("Przegladanie Ofert")
  	.exec(mapa_Polska)

  val nbUsers = Integer.getInteger("users", 10)

  setUp(scn.inject(atOnceUsers(nbUsers)).protocols(httpConf))
}
