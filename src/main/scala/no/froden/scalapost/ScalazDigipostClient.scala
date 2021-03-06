package no.froden.scalapost

import internal._
import java.io.InputStream
import scalaz.{\/, EitherT}
import concurrent.Future
import no.froden.scalapost.Implicits.FutureResultMonad
import scala.concurrent.ExecutionContext.Implicits.global

class ScalazDigipostClient(val userId: Long, certificate: InputStream, passPhrase: String)
  extends Digipost[FutureResult] with ScalazHttpService {

  override val signature = Crypto.sign(certificate, passPhrase).get

  override def failure(a: ScalaPostError): FutureResult[Nothing] = EitherT(Future(\/.left(a)))
}

