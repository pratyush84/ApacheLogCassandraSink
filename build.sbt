name := "LogReaderProject"

version := "1.0"

scalaVersion := "2.10.5"

resolvers += "confluent" at "http://packages.confluent.io/maven/"

libraryDependencies += "org.apache.spark" % "spark-streaming_2.10" % "1.6.3" % "provided"

libraryDependencies += "org.apache.spark" % "spark-core_2.10" % "1.6.3" % "provided"

libraryDependencies += "org.apache.spark" % "spark-streaming-flume_2.10" % "1.6.3"

libraryDependencies += "org.apache.spark" % "spark-streaming-kafka_2.10" % "1.6.3"

libraryDependencies += "org.apache.kafka" % "kafka_2.10" % "0.10.1.1"

libraryDependencies += "log4j" % "log4j" % "1.2.17"

libraryDependencies += "io.confluent" % "common-config" % "3.1.2"

libraryDependencies += "io.confluent" % "common-utils" % "3.1.2"

libraryDependencies += "io.confluent" % "kafka-schema-registry-client" % "3.1.2"

libraryDependencies += "io.confluent" % "kafka-avro-serializer" % "3.1.2"

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
  case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
