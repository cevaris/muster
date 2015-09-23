name := "muster"

version := "1.0"

scalaVersion := "2.10.0"

resolvers in ThisBuild ++= Seq(
  "Twitter" at "http://maven.twttr.com",
  "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
  "Finatra Repo" at "http://twitter.github.com/finatra",
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

scalacOptions in ThisBuild ++= Seq(
  "-feature",
  "-language:implicitConversions"
)

val testAndCompile = "test->test;compile->compile"

lazy val versions = new {
  val finatra = "2.0.0"
  val finagle = "6.28.0"
}

lazy val commonDeps = Seq(
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  "com.github.nscala-time" %% "nscala-time" % "2.2.0",
  "com.twitter" %% "util-collection" % "6.27.0",
  "org.apache.thrift" % "libthrift" % "0.9.2",
  "org.mockito" % "mockito-core" % "1.9.5" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.specs2" %% "specs2" % "2.3.12" % "test"
)

lazy val finatraDeps = Seq(
  "com.twitter.finatra" %% "finatra-http" % versions.finatra,
  "com.twitter.finatra" %% "finatra-http" % versions.finatra % "test" classifier "tests",
  "com.twitter.finatra" %% "finatra-httpclient" % versions.finatra,
  "com.twitter.finatra" %% "finatra-httpclient" % versions.finatra % "test" classifier "tests",
  "com.twitter.finatra" %% "finatra-logback" % s"${versions.finatra}.M1",
  "com.twitter.inject" %% "inject-app" % versions.finatra % "test",
  "com.twitter.inject" %% "inject-core" % versions.finatra % "test",
  "com.twitter.inject" %% "inject-modules" % versions.finatra % "test",
  "com.twitter.inject" %% "inject-server" % versions.finatra % "test",
  "com.twitter.inject" %% "inject-app" % versions.finatra % "test" classifier "tests",
  "com.twitter.inject" %% "inject-core" % versions.finatra % "test" classifier "tests",
  "com.twitter.inject" %% "inject-modules" % versions.finatra % "test" classifier "tests",
  "com.twitter.inject" %% "inject-server" % versions.finatra % "test" classifier "tests"
)

val finagleDeps = Seq(
  "com.twitter" %% "finagle-commons-stats" % versions.finagle,
  "com.twitter" %% "finagle-core" % versions.finagle,
  "com.twitter" %% "finagle-thrift" % versions.finagle,
  "com.twitter" %% "finagle-thriftmux" % versions.finagle
)

lazy val thrift = project.in(file("thrift"))
  .settings(
    libraryDependencies ++= commonDeps ++ finagleDeps ++ Seq(
      "com.twitter" %% "scrooge-core" % "3.20.0"
    ),
    scroogeThriftSourceFolder in Compile <<= baseDirectory {
      base => base / "src/main/thrift"
    }
  )

lazy val core = project.in(file("core"))
  .dependsOn(thrift % testAndCompile)
  .settings(
    libraryDependencies ++= commonDeps ++ finagleDeps ++ finatraDeps
  )


lazy val client = project.in(file("client"))
  .dependsOn(core % testAndCompile)