package no.froden.scalapost

import internal.{Digipost, GenericHttpService, Crypto}
import java.io.InputStream
import scalaz._
import Scalaz._

class SimpleDigipostClient(val userId: Long, certificate: InputStream, passPhrase: String)
  extends Digipost[Id] with GenericHttpService[Id] {

  override lazy val baseUrl = "https://api.digipost.no"
  val signature = Crypto.sign(certificate, passPhrase).get

  implicit def M = id

  override def failure(a: ScalaPostError): Id[Nothing] = throw new ScalaPostException(a)
}

