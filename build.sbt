name := "scalapost"

version := "1.0"

scalaVersion := "2.10.0"

resolvers ++= Seq(
  "Local maven repo" at Path.userHome.asFile.toURI.toURL + ".m2/repository"
)

libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core" % "0.9.5",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.47",
  "ch.qos.logback" % "logback-classic" % "1.0.6"
)
