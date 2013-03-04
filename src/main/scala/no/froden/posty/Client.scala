package no.froden.posty

import scalaz._
import Scalaz._
import concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import xml.{XML, Elem}
import no.froden.scalapost.ContentMD5
import java.util.{Locale, Date}
import dispatch._
import java.text.SimpleDateFormat
import java.io.StringWriter
import scala.Predef._
import scala.{util, concurrent}
import no.froden.posty.Implicits._

object Implicits {
  implicit object FutureMonad extends Monad[Future] {
    def point[A](a: => A): Future[A] = Future(a)

    def bind[A, B](fa: Future[A])(f: (A) => Future[B]): Future[B] = fa flatMap f
  }

  implicit object FutureResultMonad extends Monad[FutureResult] {
    def point[A](a: => A): FutureResult[A] = EitherT(Future(\/.right(a)))

    def bind[A, B](fa: FutureResult[A])(f: A => FutureResult[B]): FutureResult[B] = fa flatMap f
  }
}

trait ErrorReporting[M[+_]] {
  implicit def M: Monad[M]

  def success[A](a: A): M[A]
  def failure(a: String): M[Nothing]
}

trait HttpService[M[+_]] extends ErrorReporting[M] {
  implicit def M: Monad[M]

  def get(uri: String, headers: Map[String, String]): M[Elem]
  def post(uri: String, headers: Map[String, String], body: String): M[Elem]
  def post(uri: String, headers: Map[String, String], body: Array[Byte]): M[Elem]
}

trait FutureHttpService extends HttpService[FutureResult] {
  def M = FutureResultMonad

  override def get(uri: String, headers: Map[String, String]) = {
    val req = url(uri) <:< headers
    toFuture(Http(req > toXml))
  }

  override def post(uri: String, headers: Map[String, String], body: String) = {
    val req = url(uri).POST.setBody(body).setBodyEncoding("utf-8") <:< headers
    toFuture(Http(req > toXml))
  }
  override def post(uri: String, headers: Map[String, String], body: Array[Byte]) = {
    val req = url(uri).POST.setBody(body) <:< headers
    toFuture(Http(req > toXml))
  }

  override def success[A](a: A): FutureResult[A] = EitherT(Future(\/.right(a)))
  override def failure(a: String): FutureResult[Nothing] = EitherT(Future(\/.left(a)))

  def toFuture(res: Promise[Result[Elem]]): FutureResult[Elem] = {
    val p = concurrent.Promise[Result[Elem]]()
    res.fold(
      err => p.complete(util.Success(\/.left(err.getMessage))),
      succ => p.complete(util.Success(succ))
    )
    EitherT(p.future)
  }

  val toXml: Res => Result[Elem] = res =>
    if (res.getStatusCode / 100 == 2)
      \/.right(as.xml.Elem(res))
    else
      \/.left(res.getStatusCode + ": " + res.getStatusText + ": " + res.getResponseBody)
}

trait Api[M[+_]] { self: HttpService[M] =>
  lazy val baseUrl = "http://qa.api.digipost.no"

  val userId: Long
  val signature: String => String

  def createMessage(msg: Elem): M[Elem] =
    for {
      entry <- get(baseUrl, Map())
      createLink <- getLink("create_message", entry)
      delivery <- postXml(createLink, msg)
    } yield delivery

//  def deliverMessage(uri: String, content: Array[Byte]) = for {
//    finalDelivery <- postBytes(uri, content)
//  } yield finalDelivery
//
//  def sendMessage(msg: Elem, content: Array[Byte]) = for {
//    delivery <- createMessage(msg)
//    contentLink <- wrap(getLink("add_content_and_send", delivery))
//    finalDelivery <- deliverMessage(contentLink, content)
//  } yield finalDelivery

  def postBytes(uri: String, bytes: Array[Byte], contentType: String = "application/pdf"): M[Elem] = {
    val checksum = ContentMD5(bytes)
    val path = extractPath(uri)
    val date = formatDate(new Date())
    val sig = signature(stringToSign("post", path, date, userId, checksum))
    val reqHeaders = headers(date, userId, sig, checksum, contentType)
    post(uri, reqHeaders, bytes)
  }

  def postXml(uri: String, x: xml.Node): M[Elem] = {
    postBytes(uri, x.toXmlBytes(), "application/vnd.digipost-v3+xml")
  }

  def getLink(rel: String, elem: Elem): M[String] = {
    val linkOpt = elem match {
      case Links(links @ _*) => links.find(_._1 == rel).map(_._2)
      case _ => None
    }
    linkOpt.fold[M[String]](failure("Link not found: rel=" + rel))(link => success(link))
  }

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

  def headers(date: String, userId: Long, signature: String, checksum: String, contentType: String) = Map(
    "Accept" -> "application/vnd.digipost-v3+xml",
    "Date" -> date,
    "X-Digipost-UserId" -> userId.toString,
    "X-Digipost-Signature" -> signature,
    "Content-MD5" -> checksum,
    "Content-Type" -> contentType
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

class DigipostClient(val userId: Long, val signature: String => String) extends Api[FutureResult] with FutureHttpService {

}

object Client {

  def main(args: Array[String]) {
    val client = new DigipostClient(100L, _.toUpperCase)

  }
}


object Tester {

  type TestResult[+A] = String \/ A

  class DigipostTestClient(response: Elem)(implicit val M: Monad[M]) extends Api[TestResult] with HttpService[TestResult] {
    override lazy val userId = 100L
    override lazy val signature: String => String = identity

    def M = EitherT

    override def get(uri: String, headers: Map[String, String]) = <xml><link uri="jalla" rel="create_message"/></xml>
    override def post(uri: String, headers: Map[String, String], body: String) = <delivery></delivery>
    override def post(uri: String, headers: Map[String, String], body: Array[Byte]) = \/.right(response)

    def success[A](a: A) = \/.right(a)

    def failure(a: String) = \/.left(a)
  }

  def main(args: Array[String]) {
    val client = new DigipostTestClient(<xml>hei</xml>)
    val delivery = client.createMessage(Message("Dette er en test", DigipostAddress("frode.nerbråten#000")))
    println(delivery)
  }
}
