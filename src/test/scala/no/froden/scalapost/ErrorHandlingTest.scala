package no.froden.scalapost

import org.scalatest.FunSuite
import dispatch.Http.promise
import xml.Elem
import scalaz.{Id, EitherT, \/}

class ErrorHandlingTest extends FunSuite {

  trait TestHttpService extends HttpService {
    def get(uri: String, headers: Map[String, String]) = wrap(\/.left("feil fra get"))
    def post(uri: String, headers: Map[String, String], body: String) = wrap(\/.left("feil fra post1"))
    def post(uri: String, headers: Map[String, String], body: Array[Byte]) = wrap(\/.left("feil fra post2"))
  }

  object TestApi extends Digipost with TestHttpService {
    lazy val user = 123L
    lazy val sign: String => String = "**signed**" + _ + "**signed**"
  }

  test("Should short circuit on http error") {
    val res = TestApi.createMessage(Message("Test1", DigipostAddress("sindre.bartnes.nordbø#5B53")))
    val error = res.run().fold(identity, _ => "ok")
    assert(error === "feil fra get")
  }
}
