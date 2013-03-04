package no.froden.posty

import xml.Elem
import scalaz._
import Scalaz._

trait Digipost[M[+ _]] extends Api[M] {
  self: HttpService[M] =>

  def createMessage(msg: Elem): M[Elem] =
    for {
      entry <- getXml()
      createLink <- getLink("create_message", entry)
      delivery <- postXml(createLink, msg)
    } yield delivery

  def deliverMessage(uri: String, content: Array[Byte]): M[Elem] = for {
    finalDelivery <- postBytes(uri, content)
  } yield finalDelivery

  def sendMessage(msg: Elem, content: Array[Byte]) = for {
    delivery <- createMessage(msg)
    contentLink <- getLink("add_content_and_send", delivery)
    finalDelivery <- deliverMessage(contentLink, content)
  } yield finalDelivery

}
