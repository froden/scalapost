package no.froden.posty

import xml.{XML, Elem}
import java.util.{Locale, Date}
import dispatch.{Http, RawUri}
import java.text.SimpleDateFormat
import java.io.StringWriter

trait Api[M[+ _]] {
  self: HttpService[M] =>
  lazy val baseUrl = "http://qa.api.digipost.no"

  val userId: Long
  val signature: String => String

  def postBytes(uri: String, bytes: Array[Byte], contentType: String): M[Elem] = {
    val checksum = ContentMD5(bytes)
    val path = extractPath(uri)
    val date = formatDate(new Date())
    val sig = signature(stringToSign("post", path, date, userId, checksum))
    val reqHeaders = headers(date, userId, sig) ++ postHeaders(checksum, contentType)
    post(uri, reqHeaders, bytes)
  }

  def postXml(uri: String, x: xml.Node): M[Elem] = {
    postBytes(uri, x.toXmlBytes(), "application/vnd.digipost-v3+xml")
  }

  def getXml(uri: String = baseUrl): M[Elem] = {
    val path = extractPath(uri)
    val date = formatDate(new Date())
    val sig = signature(stringToSign("get", path, date, userId))
    get(uri, headers(date, userId, sig))
  }

  def getLink(rel: String, elem: Elem): M[String] = {
    val linkOpt = elem match {
      case Links(links@_*) => links.find(_._1 == rel).map(_._2)
      case _ => None
    }
    linkOpt.fold[M[String]](failure("Link not found: rel=" + rel))(link => success(link))
  }

  def stringToSign(method: String, path: String, date: String, userId: Long, contentMD5: String = "") = {
    val str = new StringBuilder()
    str ++= method.toUpperCase ++= "\n"
    str ++= path ++= "\n"
    if (!contentMD5.isEmpty) str ++= "content-md5: " ++= contentMD5 ++= "\n"
    str ++= "date: " ++= date ++= "\n"
    str ++= "x-digipost-userid: " ++= userId.toString ++= "\n"
    str ++= "\n"
    str.toString()
  }

  def postHeaders(checksum: String, contentType: String) = Map(
    "Content-MD5" -> checksum,
    "Content-Type" -> contentType
  )

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

  def formatDate(date: Date) = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).format(date)

  implicit class XmlWriter(x: xml.Node) {
    def toXmlString(): String = {
      val writer = new StringWriter()
      XML.write(writer, x, "UTF-8", true, null)
      writer.toString
    }

    def toXmlBytes(): Array[Byte] = toXmlString().getBytes("utf-8")
  }

  def shutdown() = Http.shutdown()
}

