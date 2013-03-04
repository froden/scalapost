package no.froden.posty

import dispatch._
import scalaz.{\/, EitherT}
import concurrent.Future
import xml.Elem
import scala.concurrent.ExecutionContext.Implicits.global
import no.froden.posty.Implicits._

trait FutureHttpService extends HttpService[FutureResult] with ErrorReporting[FutureResult] {
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
