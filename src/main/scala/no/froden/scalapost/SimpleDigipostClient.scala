package no.froden.scalapost

import internal.{Digipost, SimpleHttpService, Crypto}
import java.io.InputStream
import scalaz._
import Scalaz._

class SimpleDigipostClient(val userId: Long, certificate: InputStream, passPhrase: String)
  extends Digipost[Id] with SimpleHttpService {

  override val signature = Crypto.sign(certificate, passPhrase).get

  override def failure(a: ScalaPostError): Id[Nothing] = throw new ScalaPostException(a)

  //For Java-API to play nicely
  override def sendPdfMessage(msg: Message, pdf: Array[Byte]): MessageDelivery = super.sendPdfMessage(msg, pdf)
}

