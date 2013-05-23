package no.froden.scalapost.internal

import java.io.InputStream
import java.security.{Security, KeyStore, PrivateKey, Signature}
import java.security.interfaces.RSAPrivateCrtKey
import scala.Predef.String
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.jce.provider.BouncyCastleProvider
import util.{Failure, Try}

object Crypto {
  def loadKeyFromP12(certificateStream: InputStream, password: String): Try[PrivateKey] =
    if (certificateStream == null)
      Failure(new IllegalArgumentException("certificate was null"))
    else
      Try {
        val keyStore: KeyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(certificateStream, password.toCharArray)
        val onlyKeyAlias: String = keyStore.aliases.nextElement
        keyStore.getKey(onlyKeyAlias, password.toCharArray).asInstanceOf[RSAPrivateCrtKey]
      }

  def initSignature() = Try {
    Security.addProvider(new BouncyCastleProvider())
  }

  def sign(certificateStream: InputStream, password: String): Try[String => String] =
    for {
      privateKey <- loadKeyFromP12(certificateStream, password)
      _ <- initSignature()
    } yield (messageToSign: String) => {
      val instance = Signature.getInstance("SHA256WithRSAEncryption")
      instance.initSign(privateKey)
      instance.update(messageToSign.getBytes)
      new String(Base64.encode(instance.sign))
    }

}


