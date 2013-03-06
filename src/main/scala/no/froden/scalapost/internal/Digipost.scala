package no.froden.scalapost.internal

import scalaz._
import Scalaz._
import no.froden.scalapost.{MessageDelivery, Message}
import xml.Elem

trait Digipost[M[+_]] extends Api[M] {
  self: HttpService[M] with ErrorReporting[M] =>

  def sendPdfMessage(msg: Message, pdf: Array[Byte]): M[MessageDelivery] = sendMessage(msg, pdf, "application/pdf")

  protected def sendMessage(msg: Message, content: Array[Byte], contentType: String): M[MessageDelivery] =
    for {
      delivery <- createMessage(msg)
      contentLink <- getLink("add_content_and_send", delivery)
      finalDelivery <- deliverMessage(contentLink, content, contentType)
    } yield MessageDelivery(finalDelivery)

  protected def createMessage(msg: Message): M[Elem] =
    for {
      entry <- getXml()
      createLink <- getLink("create_message", entry)
      delivery <- postMessage(createLink, msg)
    } yield delivery

  protected def deliverMessage(uri: String, content: Array[Byte], contentType: String): M[Elem] =
    for {
      finalDelivery <- postBytes(uri, content, contentType)
    } yield finalDelivery
}
