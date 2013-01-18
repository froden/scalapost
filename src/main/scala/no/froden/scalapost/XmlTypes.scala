package no.froden.scalapost

import xml.Elem


object Message {
  def apply(subject: String, recipient: Elem) = {
    <message xmlns="http://api.digipost.no/schema/v3">
      <message-id>{(math.random*1000).toString}</message-id>
      <subject>
        {subject}
      </subject>
      <recipient>
        {recipient}
      </recipient>
      <sms-notification/>
      <authentication-level>PASSWORD</authentication-level>
      <sensitivity-level>NORMAL</sensitivity-level>
    </message>
  }
}

object DigipostAddress {
  def apply(digipostAddress: String) = {
    <digipost-address>{digipostAddress}</digipost-address>
  }
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