package no.froden.scalapost.internal

import scalaz._
import Scalaz._
import no.froden.scalapost.{MessageDelivery, Message}

trait Digipost[M[+_]] extends Api[M] {
  self: HttpService[M] with ErrorReporting[M] =>

  def createMessage(msg: Message): M[MessageDelivery] =
    for {
      entry <- getXml()
      createLink <- getLink("create_message", entry)
      delivery <- postMessage(createLink, msg)
    } yield MessageDelivery(delivery)

  def deliverMessage(uri: String, content: Array[Byte], contentType: String): M[MessageDelivery] = for {
    finalDelivery <- postBytes(uri, content, contentType)
  } yield MessageDelivery(finalDelivery)

  def sendPdfMessage(msg: Message, pdf: Array[Byte]) = sendMessage(msg, pdf, "application/pdf")

  def sendMessage(msg: Message, content: Array[Byte], contentType: String): M[MessageDelivery] = for {
    delivery <- createMessage(msg)
    contentLink <- getLink("add_content_and_send", delivery.xml)
    finalDelivery <- deliverMessage(contentLink, content, contentType)
  } yield finalDelivery

}
