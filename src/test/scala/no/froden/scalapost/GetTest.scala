package no.froden.scalapost

import org.scalatest.FunSuite
import dispatch.Http.promise

class GetTest extends FunSuite {

  trait TestHttpService extends HttpService {
    def get(uri: String, headers: Map[String, String]): Res = promise(Left("feil"))
    def post(uri: String, headers: Map[String, String], body: String): Res = promise(Left("feil"))
    def post(uri: String, headers: Map[String, String], body: Array[Byte]): Res = promise(Left("feil"))
  }

  object TestApi extends Api with TestHttpService {
    lazy val user = 123L
    lazy val sign = (s: String) => "ssss" + s + "sss"
  }

  test("jeje") {
    val res = TestApi.createMessage(Message("Test1", DigipostAddress("sindre.bartnes.nordbø#5B53")))
    println(res())
  }
}
