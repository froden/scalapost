package no.froden.scalapost

import internal.IO
import scala.Predef._
import scala.util
import util.Random

object SimpleClient {

   def main(args: Array[String]) {
     val cert = getClass.getResourceAsStream("/hackaton7-test.p12")
     val client = new SimpleDigipostClient(179079L, cert, "Qwer1234!")

     val res = client.sendPdfMessage(
       Message("msg:" + Random.nextInt(1000), "Scalapost test", DigipostAddress("frode.nerbråten#0000")),
       IO.classpathResource("/About Stacks.pdf"))
     println(res)
   }
 }


