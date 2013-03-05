package no.froden.scalapost

import xml.{NodeSeq, Elem}

case class Message(messageId: String, subject:String, recipient: Recipient) {
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

case class MessageDelivery(xml: Elem)

trait Recipient {
  def toXml: NodeSeq
}

case class DigipostAddress(digipostAddress: String) extends Recipient {
 def toXml = <digipost-address>{digipostAddress}</digipost-address>
}

case class PersonalIdentificationNumber(pin: String) extends Recipient {
  def toXml = <personal-identification-number>{pin}</personal-identification-number>

}

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