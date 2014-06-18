name := "ckan-api"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
 "ckan-client" % "ckan-client" % "0.0.1",
 "com.fasterxml.jackson.dataformat" % "jackson-dataformat-csv" % "2.4.0",
  "org.apache.poi" % "poi" % "3.10-FINAL",
  "org.apache.poi" % "poi-ooxml" % "3.10-FINAL"
)     

play.Project.playJavaSettings
