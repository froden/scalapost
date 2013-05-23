package no.froden.scalapost.examples

import scala.Predef._
import no.froden.scalapost.{Message, DigipostAddress, SimpleDigipostClient}
import no.froden.scalapost.internal.IO

object SimpleExampleClient {

   def main(args: Array[String]) {
     val cert = getClass.getResourceAsStream("/certificate.p12")
     val client = new SimpleDigipostClient(100L, cert, "password")

     val res = client.sendPdfMessage(
       Message("msg1", "Scalapost test", DigipostAddress("test.testsson#0000")),
       IO.classpathResource("/content.pdf"))
     println(res)
   }
 }


