package no.froden.scalapost

import scalaz._
import Scalaz._
import concurrent.{Await, Future}
import concurrent.ExecutionContext.Implicits.global
import concurrent.duration.Duration
import no.froden.scalapost.ImplicitFrode.FutureMonad


trait FrodeService[M[_]] {
  implicit def M: Monad[M]

  def hei: M[String]
}

class TestFrode[M[_]](implicit val M: Monad[M]) extends FrodeService[M] {
  def hei = M.point("Hei på deg!")
}

class RealFrode extends FrodeService[Option] {
  def M = Monad[Option]

  def hei = Some("Hei på deg option")
}
object ImplicitFrode {
  implicit object FutureMonad extends Monad[Future] {
    def point[A](a: => A): Future[A] = Future(a)

    def bind[A, B](fa: Future[A])(f: (A) => Future[B]): Future[B] = fa flatMap f
  }
}

class FutureFrode extends FrodeService[Future] {
  def M = FutureMonad

  def hei = Future("Hei på deg future")
}

object Jeje extends App {
  import ImplicitFrode._

  val frode = new TestFrode[List]()
//  val frode = new RealFrode()
//  val frode = new FutureFrode()

  frode.hei
  val res = for {
    hei <- frode.hei
  } yield println(hei)

//  Await.ready(res, Duration.Inf)
}