package no.froden.scalapost

import scala.xml.{Node, NodeSeq, Elem}

trait XmlMessage {
  def toXml: Node
}

case class Invoice(messageId: String, subject:String, recipient: Recipient, kid: String, amount: String, account: String, dueDate: String) extends XmlMessage {
  def toXml = {
    <invoice xmlns="http://api.digipost.no/schema/v3">
      <message-id>{messageId}</message-id>
      <subject>{subject}</subject>
      <recipient>{recipient.toXml}</recipient>
      <sms-notification/>
      <authentication-level>PASSWORD</authentication-level>
      <sensitivity-level>NORMAL</sensitivity-level>
      <kid>{kid}</kid>
      <amount>{amount}</amount>
      <account>{account}</account>
      <due-date>{dueDate}</due-date>
    </invoice>
  }
}

case class Message(messageId: String, subject:String, recipient: Recipient) extends XmlMessage {
  def toXml =
    <message xmlns="http://api.digipost.no/schema/v3">
      <message-id>{messageId}</message-id>
      <subject>{subject}</subject>
      <recipient>{recipient.toXml}</recipient>
      <sms-notification/>
      <authentication-level>PASSWORD</authentication-level>
      <sensitivity-level>NORMAL</sensitivity-level>
    </message>
}

case class MessageDelivery(messageId: String, deliveryMethod: String, status: String)
object MessageDelivery {
  def apply(xml: Elem): MessageDelivery = {
    val root = xml \\ "message-delivery"
    val messageId = (root \ "message-id").text
    val deliveryMethod = (root \ "delivery-method").text
    val status = (root \ "status").text
    MessageDelivery(messageId, deliveryMethod, status)
  }
}

trait Recipient {
  def toXml: NodeSeq
}

case class DigipostAddress(digipostAddress: String) extends Recipient {
 def toXml = <digipost-address>{digipostAddress}</digipost-address>
}

case class PersonalIdentificationNumber(pin: String) extends Recipient {
  def toXml = <personal-identification-number>{pin}</personal-identification-number>
}

case class ScalaPostException(err: ScalaPostError) extends Exception(err.toString)

trait ScalaPostError
case class HttpError(httpStatus: Int, httpMsg: String, body: String = "") extends ScalaPostError
case class ErrorMessage(msg: String) extends ScalaPostError
case class ErrorThrowable(ex: Throwable) extends ScalaPostError

object Links {

  def unapplySeq(elem: Elem): Option[Seq[(String, String)]] = {
    val linkNodes = elem \\ "link"
    if (linkNodes.isEmpty)
      None
    else
      Some(for {
        linkNode <- linkNodes
        uri <- linkNode.attribute("uri")
        rel <- linkNode.attribute("rel")
      } yield (rel.text.replaceAll(".*/", ""), uri.text))
  }
}