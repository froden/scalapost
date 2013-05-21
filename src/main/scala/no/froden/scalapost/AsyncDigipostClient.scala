package no.froden.scalapost

import internal._
import java.io.InputStream
import concurrent.Future

class AsyncDigipostClient(val userId: Long, certificate: InputStream, passPhrase: String)
  extends Digipost[Future] with FutureHttpService with ErrorReporting[Future] {

  override val signature = Crypto.sign(certificate, passPhrase).get

  override def failure(a: ScalaPostError) = Future.failed(new ScalaPostException(a))
}

