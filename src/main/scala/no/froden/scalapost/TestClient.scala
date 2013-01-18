package no.froden.scalapost

import dispatch.Http.promise

object TestClient extends App {

  val logAndExit = (error: Throwable) => {
    println(error)
    sys.exit(1)
  }

  val sign = Crypto.sign(getClass.getResourceAsStream("/certificate.p12"), "Qwer1234!").fold(logAndExit, identity)

  val api = Digipost(5, sign)

  val res = for {
    entry <- api.get().right
    createLink <- promise(api.getLink("create_message", entry)).right
    delivery <- api.post(createLink, Message("Hei pæøå deg", DigipostAddress("sindre.bartnes.nordbø#5B53"))).right
    contentLink <- promise(api.getLink("add_content_and_send", delivery)).right
    finalDelivery <- api.post(contentLink, IO.classpathResource("About Stacks.pdf")).right
  } yield finalDelivery

  println(res())

  api.shutdown()
}
