package no.froden

import scalaz.{EitherT, \/}
import concurrent.Future

package object scalapost {

  type Result[+A] = ScalaPostError \/ A

  type FutureResult[+B] = EitherT[Future, ScalaPostError, B]
}
