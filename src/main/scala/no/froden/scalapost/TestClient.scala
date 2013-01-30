package no.froden.scalapost

import dispatch.Http.promise
import org.slf4j.LoggerFactory

object TestClient extends App with DispatchHttpService {

  val log = LoggerFactory.getLogger(getClass)

  val logAndExit = (error: Throwable) => {
    log.error(error.toString)
    sys.exit(1)
  }

  val sign = Crypto.sign(getClass.getResourceAsStream("/5.p12"), "Qwer1234").fold(logAndExit, identity)

  val api = Digipost(5, sign)

  val res = for {
    entry <- api.get().right
    createLink <- promise(api.getLink("create_message", entry)).right
    delivery <- api.post(createLink, Message("Hei pæøå deg", DigipostAddress("sindre.bartnes.nordbø#5B53"))).right
    contentLink <- promise(api.getLink("add_content_and_send", delivery)).right
    finalDelivery <- api.post(contentLink, IO.classpathResource("About Stacks.pdf")).right
  } yield finalDelivery

  log.info(res().toString)

  api.shutdown()
}
