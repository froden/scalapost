package no.froden.scalapost.internal

import dispatch._
import xml.Elem
import no.froden.scalapost.{ScalaPostException, HttpError}
import scala.concurrent.ExecutionContext.Implicits.global
import no.froden.scalapost.Implicits.FutureMonad

trait FutureHttpService extends HttpService[Future] {

  override implicit def M = FutureMonad

  override def get(uri: String, headers: Map[String, String]) =
    HttpHelper.get(uri, headers)

  override def post(uri: String, headers: Map[String, String], body: String) =
    HttpHelper.post(uri, headers, body)

  override def post(uri: String, headers: Map[String, String], body: Array[Byte]) =
    HttpHelper.post(uri, headers, body)
}
