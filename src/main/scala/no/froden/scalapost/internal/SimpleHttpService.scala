package no.froden.scalapost.internal

import dispatch._
import scalaz._
import Scalaz._

trait SimpleHttpService extends HttpService[Id] {

  implicit override def M = id

  override def get(uri: String, headers: Map[String, String]) =
    HttpHelper.get(uri, headers).apply()

  override def post(uri: String, headers: Map[String, String], body: String) =
    HttpHelper.post(uri, headers, body).apply()

  override def post(uri: String, headers: Map[String, String], body: Array[Byte]) =
    HttpHelper.post(uri, headers, body).apply()
}
