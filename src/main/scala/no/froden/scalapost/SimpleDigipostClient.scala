package no.froden.scalapost

import java.io.InputStream
import scalaz._
import Scalaz._

class SimpleDigipostClient(val userId: Long, certificate: InputStream, passPhrase: String)
  extends Digipost[Id] with GenericHttpService[Id] with ErrorReporting[Id] {

  override lazy val baseUrl = "https://api.digipost.no"
  val signature = Crypto.sign(certificate, passPhrase).get

  implicit def M = id

  def success[A](a: A): Scalaz.Id[A] = a

  def failure(a: String): Scalaz.Id[Nothing] = throw new RuntimeException(a)
}

