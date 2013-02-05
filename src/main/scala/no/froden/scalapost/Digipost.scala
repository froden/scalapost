package no.froden.scalapost

import xml.Elem

trait Digipost extends Api {

  def createMessage(msg: Elem) = for {
    entry <- get()
    createLink <- wrap(getLink("create_message", entry))
    delivery <- postXml(createLink, msg)
  } yield delivery

  def deliverMessage(uri: String, content: Array[Byte]) = for {
    finalDelivery <- postBytes(uri, content)
  } yield finalDelivery

  def sendMessage(msg: Elem, content: Array[Byte]) = for {
    delivery <- createMessage(msg)
    contentLink <- wrap(getLink("add_content_and_send", delivery))
    finalDelivery <- deliverMessage(contentLink, content)
  } yield finalDelivery

}

object Digipost {

  def apply(userId: Long, createSignature: String => String) = new Object with Digipost with DispatchHttpService {
    lazy override val user = userId
    lazy override val sign = createSignature
  }
}