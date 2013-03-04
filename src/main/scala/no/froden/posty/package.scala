package no.froden

import scalaz.{Monad, EitherT, \/}
import concurrent.Future

package object posty {

  type Result[+A] = String \/ A

  type FutureResult[+B] = EitherT[Future, String, B]
}
