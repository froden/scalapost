package no.froden.scalapost.examples

import concurrent.{duration, Await}
import scala.Predef._
import no.froden.scalapost.{Message, DigipostAddress, AsyncDigipostClient}
import no.froden.scalapost.internal.IO

object AsyncExampleClient {

   def main(args: Array[String]) {
     val cert = getClass.getResourceAsStream("/certificate.p12")
     val client = new AsyncDigipostClient(100L, cert, "password")

     val fres = client.sendPdfMessage(
       Message("msg1", "Scalapost test", DigipostAddress("test.testsson#0000")),
       IO.classpathResource("/content.pdf"))
     val res = Await.result(fres, duration.Duration.Inf)
     println(res)
   }
 }


