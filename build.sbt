name := "hackkit"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions += "-target:jvm-1.7"

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

javaOptions in Test += "-Xmx2G"

testOptions in Test += Tests.Argument("-oF")

fork := true

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/stew/snapshots"

libraryDependencies ++= Seq (
  "org.zeromq" % "jeromq" % "0.3.6",
  "io.spray" %%  "spray-json" % "1.3.2",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "com.github.pathikrit" %% "better-files" % "2.14.0",
  "org.scalaz" %% "scalaz-core" % "7.2.4",
  "org.apache.spark" %% "spark-core" % "2.0.0" % "provided",
  "org.typelevel" %% "scalaz-outlaws" % "0.2",
  "org.apache.spark" %% "spark-sql" % "2.0.0" % "provided",
  "org.apache.spark" %% "spark-hive" % "2.0.0" % "provided",
  "org.apache.spark" %% "spark-mllib" % "2.0.0" % "provided",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.storm-enroute" %% "scalameter" % "0.7" % "test" // ScalaMeter version is set in version.sbt
)

testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")

parallelExecution in Test := false

