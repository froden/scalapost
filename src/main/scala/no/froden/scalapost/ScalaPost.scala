package no.froden.scalapost

import dispatch._
import xml.{XML, Elem}
import java.util.{Locale, Date}
import java.text.SimpleDateFormat
import com.ning.http.client.RequestBuilder
import java.security.MessageDigest
import org.bouncycastle.util.encoders.Base64
import java.io.StringWriter

case class ApiError(status: Int, body: Elem) extends Exception("Status=%s, %s".format(status, body))

object ResponseHandlers {

}

object ScalaPost {

  val toXml: Res => Elem = res =>
    if (res.getStatusCode / 100 == 2)
      as.xml.Elem(res)
    else
      throw ApiError(res.getStatusCode, if (res.hasResponseBody) as.xml.Elem(res) else <error />)

  def formatDate(date: Date) = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).format(date)

  def stringToSign(method: String, path: String, date: String, userId: Long, contentMD5: String = "") = {
    val str = new StringBuilder()
    str.append(method.toUpperCase + "\n")
    str.append(path + "\n")
    if (!contentMD5.isEmpty) str.append("content-md5: ").append(contentMD5 + "\n")
    str.append("date: ").append(date + "\n")
    str.append("x-digipost-userid: " ).append(userId.toString + "\n")
    str.append("\n")
    println(str.toString())
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

  def request(method: String, userId: Long, uri: String, sign: String => String, date: String, contentMD5: String = "") = {
    val path = extractPath(uri)
    val signature = sign(stringToSign(method, path, date, userId, contentMD5))
    url(uri) <:< headers(date, userId, signature)
  }
}

object ContentMD5 {
  lazy val md5Digester = MessageDigest.getInstance("MD5")

  def apply(data: String): String = apply(data.getBytes("utf-8"))
  def apply(data: Array[Byte]): String = new String(Base64.encode(md5Digester.digest(data)), "utf-8")
}

trait Api {
  import ScalaPost._

  val baseUrl = "http://localhost:8282"
//  val baseUrl = "https://qa.digipost.no"

  val user: Long
  val sign: String => String

  def get(uri: String = baseUrl) = {
    val req = request("get", user, uri, sign, formatDate(new Date()))
    val res = Http(req > toXml).either
    for (err <- res.left) yield err.getMessage
  }

  implicit class XmlWriter(x: xml.Node) {
    def toXmlString(): String = {
      val writer = new StringWriter()
      XML.write(writer, x, "UTF-8", true, null)
      writer.toString
    }
  }

  def post(uri: String, bytes: Array[Byte], contentType: String = "application/pdf"): Promise[Either[String, Elem]] = {
    val checksum = ContentMD5(bytes)
    val path = extractPath(uri)
    val date = formatDate(new Date())
    val signature = sign(stringToSign("post", path, date, user, checksum))
    val reqHeaders = headers(date, user, signature) ++
      Map("Content-MD5" -> checksum, "Content-Type" -> contentType)
    val res = Http(url(uri).POST.setBody(bytes).setBodyEncoding("utf-8") <:< reqHeaders > toXml).either
    for (err <- res.left) yield err.getMessage
  }

  def post(uri: String, x: xml.Node): Promise[Either[String, Elem]] = {
    val body = x.toXmlString()
    println(body)
    post(uri, body.getBytes("utf-8"), "application/vnd.digipost-v3+xml; charset=UTF-8")
  }

  def getLink(rel: String, elem: Elem) = {
    println(elem)
    val link = elem match {
      case Links(links @ _*) => links.find(_._1 == rel).map(_._2)
      case _ => None
    }
    println(link)
    link.toRight(rel + " link not found")
  }

  def shutdown() = Http.shutdown()
}

object Digipost {
  def apply(userId: Long, createSignature: String => String) = new Object with Api {
    lazy override val user = userId
    lazy override val sign = createSignature
  }
}