package no.froden.scalapost

import java.io.InputStream

class ScalazDigipostClient(val userId: Long, certificate: InputStream, passPhrase: String) extends Digipost[FutureResult] with FutureHttpService {
  override lazy val baseUrl = "https://api.digipost.no"
  val signature = Crypto.sign(certificate, passPhrase).get
}

