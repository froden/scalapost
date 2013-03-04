package no.froden.posty

import java.io.InputStream

class DigipostClient(val userId: Long, certificate: InputStream, passPhrase: String) extends Api[FutureResult] with FutureHttpService {
  override lazy val baseUrl = "https://api.digipost.no"
  val signature = Crypto.sign(certificate, passPhrase).fold(ex => throw ex, identity)
}
