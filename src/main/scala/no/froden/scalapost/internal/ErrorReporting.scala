package no.froden.scalapost.internal

import scalaz.Monad
import no.froden.scalapost.{ErrorMessage, ScalaPostError}

trait ErrorReporting[M[+_]] {
  implicit def M: Monad[M]

  def failure(err: ScalaPostError): M[Nothing]
  def failure(err: String): M[Nothing] = failure(ErrorMessage(err))
}
