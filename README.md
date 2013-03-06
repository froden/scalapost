scalapost
=========

Scala/Java Client API for sending messages in Digipost


### Usage

Requirements: Scala 2.10.0 or Java 7

#### Scala synchronous
```scala
val cert = getClass.getResourceAsStream("/certificate.p12")
val client = new SimpleDigipostClient(100L, cert, "password")

val res = client.sendPdfMessage(
 Message("msg1", "Scalapost test", DigipostAddress("test.testsson#0000")),
 IO.classpathResource("/content.pdf"))
println(res)
```

#### Scala asynchronous (using scala.concurrent.Futures)
```scala
val cert = getClass.getResourceAsStream("/certificate.p12")
val client = new AsyncDigipostClient(100L, cert, "password")

val fres = client.sendPdfMessage(
 Message("msg1", "Scalapost test", DigipostAddress("test.testsson#0000")),
 IO.classpathResource("/content.pdf"))
val res = Await.result(fres, duration.Duration.Inf)
println(res)
```

#### Java synchronous
```java
InputStream cert = JavaExampleClient.class.getResourceAsStream("/certificate.p12");
SimpleDigipostClient client = new SimpleDigipostClient(100L, cert, "password");
Message message = new Message("msg1", "Scalapost for Java", new DigipostAddress("test.testsson#0000"));
MessageDelivery res = client.sendPdfMessage(message, IO.classpathResource("/content.pdf"));
System.out.println(res);
```
