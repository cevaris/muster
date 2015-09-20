name := "muster"

version := "1.0"

scalaVersion := "2.10.5"


resolvers ++= Seq(
  "twitter" at "http://maven.twttr.com",
  "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  "com.twitter" %% "finagle-core" % "6.28.0",
  "com.twitter" %% "finagle-thrift" % "6.24.0",
  "com.twitter" %% "util-collection" % "6.27.0",
  "org.apache.thrift" % "libthrift" % "0.9.1",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

lazy val thrift = project.in(file("thrift"))
  .settings(
    scroogeThriftSourceFolder in Compile <<= baseDirectory {
      base => base / "src/main/thrift"
    }
  )
  .settings(
    libraryDependencies ++= Seq(
      "com.twitter" %% "scrooge-core" % "3.20.0",
      "org.apache.thrift" % "libthrift" % "0.9.1"
    )
  )

lazy val core = project.in(file("core"))
  .dependsOn(thrift % "test->test;compile->compile")
