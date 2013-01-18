package no.froden.scalapost

import java.io.BufferedInputStream

object IO {
  def classpathResource(name: String) = {
    val bis = new BufferedInputStream(getClass.getClassLoader.getResourceAsStream(name))
    Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toArray
  }
}
