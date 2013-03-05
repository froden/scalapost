package no.froden.scalapost.examples

import concurrent.{duration, Await}
import scala.Predef._
import scala.util
import util.Random
import no.froden.scalapost.{DigipostAddress, Message, ScalazDigipostClient}
import no.froden.scalapost.internal.IO

object ScalazTestClient {

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


