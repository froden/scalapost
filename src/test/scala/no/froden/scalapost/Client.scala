package no.froden.scalapost

import concurrent.{duration, Await}
import internal.IO
import scala.Predef._
import scala.util
import util.Random

object Client {

  def main(args: Array[String]) {
    val cert = getClass.getResourceAsStream("/hackaton7-test.p12")
    val client = new ScalazDigipostClient(179079L, cert, "Qwer1234!")

    val fres = client.sendPdfMessage(
      Message("msg:" + Random.nextInt(1000), "Scalapost test", DigipostAddress("frode.nerbråten#0000")),
      IO.classpathResource("/About Stacks.pdf"))
    val res = Await.result(fres.run, duration.Duration.Inf)
    println(res)
  }
}


