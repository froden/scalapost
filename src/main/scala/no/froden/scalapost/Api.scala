package no.froden.scalapost

import xml.{XML, Elem}
import java.util.{Locale, Date}
import java.text.SimpleDateFormat
import dispatch.{Http, RawUri}
import java.io.StringWriter

import scalaz._
import Scalaz._

case class ApiError(status: Int, body: Elem) extends Exception("Status=%s, %s".format(status, body))

trait Api extends HttpService {
  //  val baseUrl = "http://localhost:8282"
  val baseUrl = "https://qa.api.digipost.no"

  val user: Long
  val sign: String => String

  def get(uri: String = baseUrl): PromiseResponse[Elem] = {
    val path = extractPath(uri)
    val date = formatDate(new Date())
    val signature = sign(stringToSign("get", path, date, user))
    get(uri, headers(date, user, signature))
  }

  implicit class XmlWriter(x: xml.Node) {
    def toXmlString(): String = {
      val writer = new StringWriter()
      XML.write(writer, x, "UTF-8", true, null)
      writer.toString
    }

    def toXmlBytes(): Array[Byte] = toXmlString().getBytes("utf-8")
  }

  def postBytes(uri: String, bytes: Array[Byte], contentType: String = "application/pdf"): PromiseResponse[Elem] = {
    val checksum = ContentMD5(bytes)
    val path = extractPath(uri)
    val date = formatDate(new Date())
    val signature = sign(stringToSign("post", path, date, user, checksum))
    val reqHeaders = headers(date, user, signature) ++
      Map("Content-MD5" -> checksum, "Content-Type" -> contentType)
    post(uri, reqHeaders, bytes)
  }

  def postXml(uri: String, x: xml.Node): PromiseResponse[Elem] = {
    postBytes(uri, x.toXmlBytes(), "application/vnd.digipost-v3+xml")
  }

  def getLink(rel: String, elem: Elem) = {
    val link = elem match {
      case Links(links @ _*) => links.find(_._1 == rel).map(_._2)
      case _ => None
    }
    link.toRightDisjunction(rel + " link not found")
  }

  def formatDate(date: Date) = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).format(date)

  def stringToSign(method: String, path: String, date: String, userId: Long, contentMD5: String = "") = {
    val str = new StringBuilder()
    str ++= method.toUpperCase ++= "\n"
    str ++= path ++= "\n"
    if (!contentMD5.isEmpty) str.append("content-md5: ").append(contentMD5 + "\n")
    str.append("date: ").append(date + "\n")
    str.append("x-digipost-userid: " ).append(userId.toString + "\n")
    str.append("\n")
    str.toString()
  }

  def headers(date: String, userId: Long, signature: String) = Map(
    "Accept" -> "application/vnd.digipost-v3+xml",
    "Date" -> date,
    "X-Digipost-UserId" -> userId.toString,
    "X-Digipost-Signature" -> signature
  )

  def extractPath(uri: String) = {
    val path = RawUri(uri).path.getOrElse("/")
    if (path.isEmpty) "/" else path
  }

  def shutdown() = Http.shutdown()
}
