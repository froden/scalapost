package no.froden.scalapost

import concurrent.Future
import concurrent.ExecutionContext.Implicits.global
import no.froden.scalapost.Frode2.{DispatchService}
import dispatch._
import scalaz._
import Scalaz._

object Frode2 {

  trait FrodeService[M[_]] {
//    implicit def M: Monad[M]

    def hei: M[String]
  }

//  object FutureService extends FrodeService[Future] {
//    def hei: Future[String] = Future { "Hei på deg"}
//  }

//  object OptionService extends FrodeService[Option] {
//    def hei = Some { "Hei på deg"}
//  }

  implicit object PromiseF extends Monad[Promise]{
    def point[A](a: => A): Promise[A] = Http.promise(a)

    def bind[A, B](fa: Promise[A])(f: (A) => Promise[B]): Promise[B] = fa flatMap f
  }

  type EitherPromise[A] = Promise[Either[String, A]]
  type HttpResponse[A] = EitherT[Promise, String, A]

  object DispatchService extends FrodeService[HttpResponse] {
    def hei = EitherT.eitherT {
      Http(url("https://www.vg.no") > {
        res =>
          if (res.getStatusCode == 200)
            \/.right(res.getResponseBody)
          else
            \/.left(res.getResponseBody)
      })
    }
  }
}

object Test extends App {
  import no.froden.scalapost.Frode2.PromiseF
  //    val service = FutureService
//  val service = OptionService
  val service = DispatchService

  val res = for {
    h <- service.hei
    h2 <- service.hei
  } yield (h.substring(0, 50) + h2.substring(0, 50))

  val hoho = res.run()
  println(hoho)
}