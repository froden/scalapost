package no.froden.scalapost.internal

object IO {
  def classpathResource(name: String) = {
    val bis = getClass.getResourceAsStream(name)
    Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toArray
  }
}