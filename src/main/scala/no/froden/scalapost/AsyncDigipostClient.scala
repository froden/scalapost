package no.froden.scalapost

import internal._
import java.io.InputStream
import concurrent.Future
import Implicits.FutureMonad
import scala.concurrent.ExecutionContext.Implicits.global

class AsyncDigipostClient(val userId: Long, certificate: InputStream, passPhrase: String)
  extends Digipost[Future] with GenericHttpService[Future] with ErrorReporting[Future] {

  override lazy val baseUrl = "https://api.digipost.no"
  val signature = Crypto.sign(certificate, passPhrase).get

  implicit def M = FutureMonad

  def success[A](a: A) = Future(a)

  def failure(a: String) = Future.failed(new RuntimeException(a))
}

