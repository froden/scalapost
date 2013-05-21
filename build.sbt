name := "scalapost"

version := "1.0"

scalaVersion := "2.10.1"

resolvers ++= Seq(
  "Local maven repo" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
)

libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core" % "0.10.0",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.47",
  "ch.qos.logback" % "logback-classic" % "1.0.6",
  "org.scalaz" %% "scalaz-core" % "7.0.0"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "junit" % "junit" % "4.11" % "test"
)
