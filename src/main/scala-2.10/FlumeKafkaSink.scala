import scala.io.Source
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.flume._
import org.apache.log4j.{Level, Logger}
import org.apache.spark.storage.StorageLevel
import java.net.InetSocketAddress
import org.apache.spark.streaming.kafka._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import java.util.Properties
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import io.confluent.kafka.serializers.KafkaAvroSerializer
import ApacheAccessLog.parseLogLine



object FlumePollingEventClient {

  def main(args: Array[String]) {
    if (args.length < 3) {
      System.err.println(
        "Usage: FlumePollingEventClient <host> <port> <kafkaBrokers>")
      System.exit(1)
    }

    val Array(host, port, kafkaBrokers) = args

    val batchInterval = Milliseconds(2000)

    // Create the context and set the batch size
    val sparkConf = new SparkConf().setAppName("FlumePollingEventClient").setMaster("local[6]")

    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.WARN)
    Logger.getLogger("org").setLevel(Level.WARN)
    Logger.getLogger("akka").setLevel(Level.WARN)

    val ssc = new StreamingContext(sparkConf, batchInterval)

    // Create a flume stream that polls the Spark Sink running in a Flume agent
  
    val address = Array(new InetSocketAddress(host,port.toInt))
    val stream = FlumeUtils.createPollingStream(ssc,address,StorageLevel.MEMORY_AND_DISK_SER_2, 10,5)



    val mappedLines = stream.map(e => new String(e.event.getBody.array()))

    mappedLines.foreachRDD( rdd => {
      println("# events = " + rdd.count())
      import io.confluent.kafka.serializers.KafkaAvroSerializer
      val props = new Properties()
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers)
      props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,classOf[io.confluent.kafka.serializers.KafkaAvroSerializer])
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,classOf[io.confluent.kafka.serializers.KafkaAvroSerializer])
      props.put("schema.registry.url", "http://localhost:8081")



      rdd.foreachPartition(partition => {
        partition.foreach(record1 => {
          val producer = new KafkaProducer[Object,Object](props)
          val logLine: ApacheAccessLog = parseLogLine(record1.toString)

          //val parsedRecord = parseFromLogLine(record1.toString)
          val schema: Schema = new Schema.Parser().parse(Source.fromURL(getClass.getResource("/schema.avsc")).mkString)
          val avroRecord: GenericRecord = new GenericData.Record(schema)

          val key = "key1"
          avroRecord.put("ipaddr",logLine.ipAddress)
          avroRecord.put("clientid", logLine.clientIdentd)
          avroRecord.put("userid", logLine.userId)
          avroRecord.put("datetime", logLine.dateTime)
          avroRecord.put("method", logLine.method)
          avroRecord.put("endpoint", logLine.endpoint)
          avroRecord.put("protocol", logLine.protocol)
          avroRecord.put("responsecode", logLine.responseCode)
          avroRecord.put("contentsize", logLine.contentSize)
          avroRecord.put("referrer", logLine.referrer)
          avroRecord.put("useragent", logLine.userAgent)


          val kafkaTopic = "cassTopic"
          val record = new ProducerRecord[Object, Object](kafkaTopic,key,avroRecord)
          producer.send(record)

        }

        )
      })
    })
    ssc.start()
    ssc.awaitTermination()
  }

}
