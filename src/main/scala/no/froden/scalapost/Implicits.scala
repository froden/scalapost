package no.froden.scalapost

import scalaz.{\/, EitherT, Monad}
import concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

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
