package no.froden.scalapost.internal

import no.froden.scalapost.Implicits.FutureResultMonad
import no.froden.scalapost._
import scalaz.{\/, EitherT}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait ScalazHttpService extends HttpService[FutureResult] {

  override implicit def M = FutureResultMonad

  override def get(uri: String, headers: Map[String, String]) =
    toEitherT(HttpHelper.get(uri, headers))

  override def post(uri: String, headers: Map[String, String], body: String) =
    toEitherT(HttpHelper.post(uri, headers, body))

  override def post(uri: String, headers: Map[String, String], body: Array[Byte]) =
    toEitherT(HttpHelper.post(uri, headers, body))

  def toEitherT[T](f: Future[T]) = EitherT(f.map(\/.right).recover{
    case t: Throwable => \/.left(ErrorThrowable(t))
  })
}
