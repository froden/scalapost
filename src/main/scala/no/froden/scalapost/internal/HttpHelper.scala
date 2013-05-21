package no.froden.scalapost.internal

import dispatch._
import scala.xml.Elem
import no.froden.scalapost.{HttpError, ScalaPostException}
import concurrent.ExecutionContext.Implicits.global

object HttpHelper {
  def get(uri: String, headers: Map[String, String]) =
    Http(url(uri) <:< headers > handleResponse)

  def post(uri: String, headers: Map[String, String], body: String) =
    Http(url(uri).POST.setBody(body).setBodyEncoding("utf-8") <:< headers > handleResponse)

  def post(uri: String, headers: Map[String, String], body: Array[Byte]) =
    Http(url(uri).POST.setBody(body) <:< headers > handleResponse)

  val handleResponse: Res => Elem = res =>
    if (res.getStatusCode / 100 == 2)
      as.xml.Elem(res)
    else
      throw new ScalaPostException(HttpError(res.getStatusCode, res.getStatusText, res.getResponseBody))
}
