package no.froden.scalapost

import xml.Elem
import scalaz._
import Scalaz._

trait Digipost[M[+_]] extends Api[M] {
  self: HttpService[M] with ErrorReporting[M] =>

  def createMessage(msg: Elem): M[Elem] =
    for {
      entry <- getXml()
      createLink <- getLink("create_message", entry)
      delivery <- postXml(createLink, msg)
    } yield delivery

  def deliverMessage(uri: String, content: Array[Byte], contentType: String): M[Elem] = for {
    finalDelivery <- postBytes(uri, content, contentType)
  } yield finalDelivery

  def sendPdfMessage(msg: Elem, pdf: Array[Byte]) = sendMessage(msg, pdf, "application/pdf")

  def sendMessage(msg: Elem, content: Array[Byte], contentType: String): M[Elem] = for {
    delivery <- createMessage(msg)
    contentLink <- getLink("add_content_and_send", delivery)
    finalDelivery <- deliverMessage(contentLink, content, contentType)
  } yield finalDelivery

}
