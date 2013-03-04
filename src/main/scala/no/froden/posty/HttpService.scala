package no.froden.posty

import scalaz._
import xml.Elem

trait HttpService[M[+_]] extends ErrorReporting[M] {
  implicit def M: Monad[M]

  def get(uri: String, headers: Map[String, String]): M[Elem]
  def post(uri: String, headers: Map[String, String], body: String): M[Elem]
  def post(uri: String, headers: Map[String, String], body: Array[Byte]): M[Elem]
}

trait ErrorReporting[M[+_]] {
  implicit def M: Monad[M]

  def success[A](a: A): M[A]
  def failure(a: String): M[Nothing]
}

