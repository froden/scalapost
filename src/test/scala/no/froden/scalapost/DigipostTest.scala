package no.froden.scalapost

import internal.{Digipost, ErrorReporting, HttpService}
import scalaz.{Monad, \/}
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers

@RunWith(classOf[JUnitRunner])
class DigipostTest extends FunSuite with ShouldMatchers {

  type TestResult[+A] = ScalaPostError \/ A

  class TestClient(implicit val M: Monad[TestResult])
    extends Digipost[TestResult] with HttpService[TestResult] with ErrorReporting[TestResult] {

    override lazy val userId = 100L
    override lazy val signature: String => String = identity

    override def get(uri: String, headers: Map[String, String]) = \/.right {
      <links xmlns="http://api.digipost.no/schema/v3">
        <link rel="https://api.digipost.no/relations/create_message" uri="https://api.digipost.no/messages"
              media-type="application/vnd.digipost-v3+xml"/>
      </links>
    }
    override def post(uri: String, headers: Map[String, String], body: String) = \/.right {
      <message-delivery xmlns="http://api.digipost.no/schema/v3">
        <message-id>msg:197</message-id>
        <delivery-method>DIGIPOST</delivery-method>
        <status>NOT_COMPLETE</status>
        <link rel="https://api.digipost.no/relations/self" uri="https://api.digipost.no/messages/109388"
              media-type="application/vnd.digipost-v3+xml"/>
        <link rel="https://api.digipost.no/relations/add_content_and_send"
              uri="https://api.digipost.no/messages/109388/completion" media-type="application/vnd.digipost-v3+xml"/>
        <link rel="https://api.digipost.no/relations/add_content" uri="https://api.digipost.no/messages/109388/content"
              media-type="application/vnd.digipost-v3+xml"/>
      </message-delivery>
    }

    override def post(uri: String, headers: Map[String, String], body: Array[Byte]) = \/.right {
      <message-delivery xmlns="http://api.digipost.no/schema/v3">
        <message-id>msg:197</message-id>
        <delivery-method>DIGIPOST</delivery-method>
        <status>DELIVERED</status>
        <delivered-date>2013-03-04T11:31:53.346+01:00</delivered-date>
        <link rel="https://api.digipost.no/relations/self" uri="https://api.digipost.no/messages/109388"
              media-type="application/vnd.digipost-v3+xml"/>
      </message-delivery>
    }

    override def failure(a: ScalaPostError) = \/.left(a)
  }

  test("Happy case") {
    val client = new TestClient
    val delivery = client.createMessage(Message("msg:id", "Dette er en test", DigipostAddress("frode.nerbråten#0000")))
    delivery should be ('right)
  }
}

