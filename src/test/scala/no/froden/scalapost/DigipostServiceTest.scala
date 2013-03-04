package no.froden.scalapost

import scalaz.{Monad, \/}
import xml.Elem

object DigipostServiceTest {

  type TestResult[+A] = String \/ A

  class DigipostTestClient(response: Elem)(implicit val M: Monad[TestResult]) extends Digipost[TestResult] with HttpService[TestResult] with ErrorReporting[TestResult] {
    override lazy val userId = 100L
    override lazy val signature: String => String = identity

    override def get(uri: String, headers: Map[String, String]) = \/.right(<xml><link uri="jalla" rel="create_message"/></xml>)
    override def post(uri: String, headers: Map[String, String], body: String) = \/.right(<delivery></delivery>)
    override def post(uri: String, headers: Map[String, String], body: Array[Byte]) = \/.right(response)

    def success[A](a: A) = \/.right(a)

    def failure(a: String) = \/.left(a)
  }

  def main(args: Array[String]) {
    val client = new DigipostTestClient(<xml>hei</xml>)
    val delivery = client.createMessage(Message("msg:id", "Dette er en test", DigipostAddress("frode.nerbråten#0000")))
    println(delivery)
  }
}
