package no.froden.scalapost.internal

import dispatch._
import xml.Elem
import no.froden.scalapost.{HttpError, ScalaPostError}

trait GenericHttpService[M[+_]] extends HttpService[M] with ErrorReporting[M] {

  override def get(uri: String, headers: Map[String, String]) =
    handleError(Http(url(uri) <:< headers > handleResponse)())

  override def post(uri: String, headers: Map[String, String], body: String) =
    handleError(Http(url(uri).POST.setBody(body).setBodyEncoding("utf-8") <:< headers > handleResponse)())

  override def post(uri: String, headers: Map[String, String], body: Array[Byte]) =
    handleError(Http(url(uri).POST.setBody(body) <:< headers > handleResponse)())

  case class HttpException(err: ScalaPostError) extends Exception(err.toString)

  def handleError(res: Either[HttpError, Elem]): M[Elem] = res match {
    case Right(elem) => M.point(elem)
    case Left(err) => failure(err)
  }

  val handleResponse: Res => Either[HttpError, Elem] = res =>
    if (res.getStatusCode / 100 == 2)
      Right(as.xml.Elem(res))
    else
      Left(HttpError(res.getStatusCode, res.getStatusText, res.getResponseBody))
}
