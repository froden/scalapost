package no.froden.scalapost

import org.slf4j.LoggerFactory

object TestClient2 extends App {

  val log = LoggerFactory.getLogger(getClass)

  val logAndExit = (error: Throwable) => {
    log.error(error.toString)
    sys.exit(1)
  }

  val sign = Crypto.sign(getClass.getResourceAsStream("/5.p12"), "Qwer1234!").fold(logAndExit, identity)

  val api = Digipost(309403, sign)

  val content = IO.classpathResource("About Stacks.pdf")

  val time = System.currentTimeMillis()
//  val result = for {
//    msg1 <- api.createMessage(Message("Test1", DigipostAddress("sindre.bartnes.nordbø#5B53")))
//    msg2 <- api.createMessage(Message("Test2", DigipostAddress("sindre.bartnes.nordbø#5B53")))
//    msg3 <- api.createMessage(Message("Test2", DigipostAddress("sindre.bartnes.nordbø#5B53")))
//  } yield msg1 :: msg2 :: msg3 :: Nil
//  log.info(result().toString)

  log.info((System.currentTimeMillis() - time).toString)

  api.shutdown()
}
