package no.froden.scalapost

import java.io.InputStream
import java.security.{Security, KeyStore, PrivateKey, Signature}
import java.security.interfaces.RSAPrivateCrtKey
import scala.Predef.String
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.jce.provider.BouncyCastleProvider
import util.Try

object Crypto {
  def loadKeyFromP12(certificateStream: InputStream, password: String): Try[PrivateKey] =
    Try {
      val keyStore: KeyStore = KeyStore.getInstance("PKCS12")
      keyStore.load(certificateStream, password.toCharArray)
      val onlyKeyAlias: String = keyStore.aliases.nextElement
      keyStore.getKey(onlyKeyAlias, password.toCharArray).asInstanceOf[RSAPrivateCrtKey]
    }

  def initSignature(privateKey: PrivateKey) = Try {
    Security.addProvider(new BouncyCastleProvider())
    val instance = Signature.getInstance("SHA256WithRSAEncryption")
    instance.initSign(privateKey)
    instance
  }

  def sign(certificateStream: InputStream, password: String): Try[String => String] =
    for {
      privateKey <- loadKeyFromP12(certificateStream, password)
      signature <- initSignature(privateKey)
    } yield (messageToSign: String) => {
      signature.update(messageToSign.getBytes)
      new String(Base64.encode(signature.sign))
    }

}


