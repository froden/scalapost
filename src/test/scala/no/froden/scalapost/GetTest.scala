package no.froden.scalapost

import org.scalatest.FunSuite
import dispatch.Http.promise
import xml.Elem
import scalaz.{EitherT, \/}

class GetTest extends FunSuite {

  trait TestHttpService extends HttpService {
    def get(uri: String, headers: Map[String, String]): PromiseResponse[Elem] = EitherT(promise(\/.left("feil")))
    def post(uri: String, headers: Map[String, String], body: String): PromiseResponse[Elem] = EitherT(promise(\/.left("feil")))
    def post(uri: String, headers: Map[String, String], body: Array[Byte]): PromiseResponse[Elem] = EitherT(promise(\/.left("feil")))
  }

  object TestApi extends Api with TestHttpService {
    lazy val user = 123L
    lazy val sign = (s: String) => "ssss" + s + "sss"
  }

  test("jeje") {
    val res = TestApi.createMessage(Message("Test1", DigipostAddress("sindre.bartnes.nordbø#5B53")))
    println(res.run())
  }
}
