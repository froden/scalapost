package no.froden.scalapost.internal

object IO {
  def classpathResource(name: String) = {
    val bis = getClass.getResourceAsStream(name)
    if (bis != null)
      Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toArray
    else
      throw new IllegalArgumentException("resource not found on classpath: " + name)
  }
}
