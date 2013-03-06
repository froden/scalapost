package no.froden.scalapost

import internal._
import java.io.InputStream
import concurrent.Future
import Implicits.FutureMonad

class AsyncDigipostClient(val userId: Long, certificate: InputStream, passPhrase: String)
  extends Digipost[Future] with GenericHttpService[Future] {

  override lazy val baseUrl = "https://api.digipost.no"
  val signature = Crypto.sign(certificate, passPhrase).get

  implicit def M = FutureMonad

  override def failure(a: ScalaPostError) = Future.failed(new ScalaPostException(a))
}

