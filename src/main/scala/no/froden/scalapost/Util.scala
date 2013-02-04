package no.froden.scalapost

import dispatch._
import xml.{XML, Elem}
import java.util.{Locale, Date}
import java.text.SimpleDateFormat
import java.security.MessageDigest
import org.bouncycastle.util.encoders.Base64
import java.io.StringWriter
import dispatch.Http._
import scalaz._
import Scalaz._

case class ApiError(status: Int, body: Elem) extends Exception("Status=%s, %s".format(status, body))

object Util {

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
}

object ContentMD5 {
  lazy val md5Digester = MessageDigest.getInstance("MD5")

  def apply(data: String): String = ContentMD5(data.getBytes("utf-8"))
  def apply(data: Array[Byte]): String = new String(Base64.encode(md5Digester.digest(data)), "utf-8")
}

object PromiseMonad extends Monad[Promise]{
  def point[A](a: => A): Promise[A] = Http.promise(a)

  def bind[A, B](fa: Promise[A])(f: (A) => Promise[B]): Promise[B] = fa flatMap f
}

trait HttpService {
  type PromiseResponse[+A] = EitherT[Promise, String, A]
  implicit def M = PromiseMonad

  def get(uri: String, headers: Map[String, String]): PromiseResponse[Elem]
  def post(uri: String, headers: Map[String, String], body: String): PromiseResponse[Elem]
  def post(uri: String, headers: Map[String, String], body: Array[Byte]): PromiseResponse[Elem]

  def wrap[A](value: String \/ A): PromiseResponse[A] = EitherT(M.point(value))
}

trait DispatchHttpService {
  def get(uri: String, headers: Map[String, String]) = EitherT {
    val req = url(uri) <:< headers
    Http(req > toXml)
  }

  def post(uri: String, headers: Map[String, String], body: String) = EitherT {
    val req = url(uri).POST.setBody(body).setBodyEncoding("utf-8") <:< headers
    Http(req > toXml)
  }

  def post(uri: String, headers: Map[String, String], body: Array[Byte]) = EitherT {
    val req = url(uri).POST.setBody(body) <:< headers
    Http(req > toXml)
  }

  val toXml: Res => String \/ Elem = res =>
    if (res.getStatusCode / 100 == 2)
      \/.right(as.xml.Elem(res))
    else
      \/.left(res.getStatusCode + ": " + res.getStatusText + ": " + res.getResponseBody)
}

trait Api extends HttpService {
  import Util._

//  val baseUrl = "http://localhost:8282"
  val baseUrl = "https://qa.api.digipost.no"

  val user: Long
  val sign: String => String

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

  def shutdown() = Http.shutdown()
}

object Digipost {

  def apply(userId: Long, createSignature: String => String) = new Object with Api with DispatchHttpService {
    lazy override val user = userId
    lazy override val sign = createSignature
  }
}