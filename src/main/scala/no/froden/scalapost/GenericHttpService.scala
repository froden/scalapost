package no.froden.scalapost

import dispatch._

trait GenericHttpService[M[+_]] extends HttpService[M] {

  override def get(uri: String, headers: Map[String, String]) =
    M.point(Http(url(uri) <:< headers OK as.xml.Elem)())

  override def post(uri: String, headers: Map[String, String], body: String) =
    M.point(Http(url(uri).POST.setBody(body).setBodyEncoding("utf-8") <:< headers OK as.xml.Elem)())

  override def post(uri: String, headers: Map[String, String], body: Array[Byte]) =
    M.point(Http(url(uri).POST.setBody(body) <:< headers OK as.xml.Elem)())
}
