package no.froden.scalapost

import scalaz._
import xml.Elem

trait HttpService[M[+_]] {
  implicit def M: Monad[M]

  def get(uri: String, headers: Map[String, String]): M[Elem]
  def post(uri: String, headers: Map[String, String], body: String): M[Elem]
  def post(uri: String, headers: Map[String, String], body: Array[Byte]): M[Elem]
}
