name := "CS441HW1"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.6.4",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe" % "config" % "1.3.4",
    "org.scalatest" %% "scalatest" % "3.0.8" % "test",
    "junit" % "junit" % "4.11" % Test,
    "org.cloudsimplus" % "cloudsim-plus" % "5.1.0"
)