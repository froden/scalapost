package no.froden.scalapost

import scalaz.Monad

trait ErrorReporting[M[+_]] {
  implicit def M: Monad[M]

  def success[A](a: A): M[A]
  def failure(a: String): M[Nothing]
}
