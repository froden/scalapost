package no.froden.scalapost.internal

import scalaz._
import Scalaz._
import concurrent.Future
import xml.Elem
import scala.concurrent.ExecutionContext.Implicits.global
import no.froden.scalapost._
import Implicits._

trait FutureHttpService extends GenericHttpService[FutureResult] with ErrorReporting[FutureResult] {
  def M = FutureResultMonad

  lazy val httpService = new Object with GenericHttpService[Future] {
    def M = FutureMonad
  }

  override def get(uri: String, headers: Map[String, String]) = toEitherT(httpService.get(uri, headers))

  override def post(uri: String, headers: Map[String, String], body: String) =
    toEitherT(httpService.post(uri, headers, body))

  override def post(uri: String, headers: Map[String, String], body: Array[Byte]) =
    toEitherT(httpService.post(uri, headers, body))

  def toEitherT(future: Future[Elem]) = EitherT(future.map(_.right).recover{case ex => \/.left(ex.getMessage)})

  override def success[A](a: A): FutureResult[A] = EitherT(Future(\/.right(a)))
  override def failure(a: String): FutureResult[Nothing] = EitherT(Future(\/.left(a)))
}
