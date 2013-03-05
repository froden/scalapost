package no.froden.scalapost.examples

import concurrent.{duration, Await}
import scala.Predef._
import scala.util
import util.Random
import no.froden.scalapost.{DigipostAddress, Message, ScalazDigipostClient}
import no.froden.scalapost.internal.IO

object ScalazExampleClient {

  def main(args: Array[String]) {
    val cert = getClass.getResourceAsStream("/certificate.p12")
    val client = new ScalazDigipostClient(100L, cert, "password")

    val fres = client.sendPdfMessage(
      Message("msg1", "Scalapost test", DigipostAddress("test.testsson#0000")),
      IO.classpathResource("/content.pdf"))
    val res = Await.result(fres.run, duration.Duration.Inf)
    println(res)
  }
}


