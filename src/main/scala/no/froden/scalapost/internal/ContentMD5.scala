package no.froden.scalapost.internal

import java.security.MessageDigest
import org.bouncycastle.util.encoders.Base64

object ContentMD5 {

  def apply(data: String): String = ContentMD5(data.getBytes("utf-8"))

  def apply(data: Array[Byte]): String = {
    val md5Digester = MessageDigest.getInstance("MD5")
    new String(Base64.encode(md5Digester.digest(data)), "utf-8")
  }
}
