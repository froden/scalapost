package no.froden.scalapost

object TestClient extends App {

  val logAndExit = (error: Throwable) => {
    println(error)
    sys.exit(1)
  }

  val sign = Crypto.sign(getClass.getResourceAsStream("/certificate.p12"), "Qwer1234!").fold(logAndExit, identity)

  val api = Digipost(1000, sign)

  println(api.get()())

  api.shutdown()
}
