package no.froden.scalapost

import dispatch._
import xml.Elem
import java.util.{Locale, Date}
import java.text.SimpleDateFormat
import com.ning.http.client.RequestBuilder

case class ApiError(status: Int, body: Elem) extends Exception("Status=%s, %s".format(status, body))

object ScalaPost {

  val baseUrl = "https://qa.api.digipost.no"

  val toXml: Res => Elem = res =>
    if (res.getStatusCode / 100 == 2)
      as.xml.Elem(res)
    else
      throw ApiError(res.getStatusCode, as.xml.Elem(res))

  def formatDate(date: Date) = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).format(date)

  def stringToSign(method: String, path: String, date: String, userId: Long, contentMD5: String = "") =
    s"""${method.toUpperCase}
      |$path
      |date: $date
      |x-digipost-userid: ${userId.toString}
      |
      |""".stripMargin

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

  def getRequest(userId: Long, uri: String, sign: String => String, date: String) = {
    val path = extractPath(uri)
    val signature = sign(stringToSign("get", path, date, userId))
    url(uri) <:< headers(date, userId, signature)
  }

  def get(req: RequestBuilder): Promise[Either[Throwable, Elem]] = {
    Http(req > toXml).either
  }
}

trait Api {
  import ScalaPost._

  val user: Long
  val sign: String => String

  def get(uri: String = baseUrl) = {
    val req = getRequest(1000, uri, sign, formatDate(new Date()))
    Http(req > toXml)
  }

  def shutdown() = Http.shutdown()
}

object Digipost {
  def apply(userId: Long, createSignature: String => String) = new Object with Api {
    lazy override val user = userId
    lazy override val sign = createSignature
  }
}
