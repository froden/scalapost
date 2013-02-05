package no.froden.scalapost

import scalaz.{\/, EitherT, Monad}
import dispatch._
import xml.Elem

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
