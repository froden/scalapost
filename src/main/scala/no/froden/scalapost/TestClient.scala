package no.froden.scalapost

import org.slf4j.LoggerFactory

object TestClient extends App {

  val log = LoggerFactory.getLogger(getClass)

  val logAndExit = (error: Throwable) => {
    log.error(error.toString)
    sys.exit(1)
  }

  val sign = Crypto.sign(getClass.getResourceAsStream("/5.p12"), "Qwer1234").fold(logAndExit, identity)

  val api = Digipost(5, sign)

  val res = api.createMessage(Message("Test1", DigipostAddress("sindre.bartnes.nordbø#5B53")))

  log.info(res().toString)

  api.shutdown()
}
