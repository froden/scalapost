package no.froden.posty

import java.io.InputStream
import java.security.{Security, KeyStore, PrivateKey, Signature}
import java.security.interfaces.RSAPrivateCrtKey
import util.control.Exception._
import scala.Predef.String
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.jce.provider.BouncyCastleProvider

object Crypto {
  def loadKeyFromP12(certificateStream: InputStream, password: String): Either[Throwable, PrivateKey] =
    allCatch.either {
      val keyStore: KeyStore = KeyStore.getInstance("PKCS12")
      keyStore.load(certificateStream, password.toCharArray)
      val onlyKeyAlias: String = keyStore.aliases.nextElement
      keyStore.getKey(onlyKeyAlias, password.toCharArray).asInstanceOf[RSAPrivateCrtKey]
    }

  def initSignature(privateKey: PrivateKey) = {
    Security.addProvider(new BouncyCastleProvider())
    allCatch.either {
      val instance = Signature.getInstance("SHA256WithRSAEncryption")
      instance.initSign(privateKey)
      instance
    }
  }

  def sign(certificateStream: InputStream, password: String): Either[Throwable, String => String] =
    for {
      privateKey <- loadKeyFromP12(certificateStream, password).right
      signature <- initSignature(privateKey).right
    } yield (messageToSign: String) => {
      signature.update(messageToSign.getBytes)
      new String(Base64.encode(signature.sign))
    }

}


