# ApacheLogCassandraSink
![Alt Text](https://raw.github.com/pratyush84/ApacheLogCassandraSink/master/LogReaderProject.JPG)

This sample project reads the continuous increasing apache web log file using Flume taildir source. Flume sink is spark sink where the log lines are converted to Avro format and send to Kafka broker. Confluent’s Kafka Cassandra connector is then used to store the data in Cassandra table.

## Environmental Configuration
* Java JDK 1.8.0_121
* Scala 2.10
* SBT 0.13
* Spark 1.6.3
* Confluent 3.1.2
* Flume 1.7.0
* Cassandra 5.0.1
* Confluent StreamReactor 0.2-3.0.0

## Flume Config
Following flume configuration is used:
 flume-sparkpull.conf
```
flumePullAgent.sources = nc1
flumePullAgent.sinks = log spark
flumePullAgent.channels = m1 m2

flumePullAgent.sources.nc1.type = TAILDIR
flumePullAgent.sources.nc1.positionFile = /home/pratyush04/flumeconf/taildir_position.json
flumePullAgent.sources.nc1.filegroups = f1
flumePullAgent.sources.nc1.filegroups.f1 = /opt/gen_logs/logs/access.log
flumePullAgent.sources.nc1.headers.f1.headerKey1 = value1
flumePullAgent.sources.nc1.selector.type = replicating

flumePullAgent.sinks.spark.type = org.apache.spark.streaming.flume.sink.SparkSink
flumePullAgent.sinks.spark.hostname = localhost
flumePullAgent.sinks.spark.port = 33349
flumePullAgent.sinks.log.type=logger

flumePullAgent.channels.m1.type = memory
flumePullAgent.channels.m1.capacity = 1000
flumePullAgent.channels.m1.transactionCapacity = 100

flumePullAgent.channels.m2.type = memory
flumePullAgent.channels.m2.capacity = 1000
flumePullAgent.channels.m2.transactionCapacity = 100

flumePullAgent.sources.nc1.channels = m1 m2
flumePullAgent.sinks.log.channel=m1
flumePullAgent.sinks.spark.channel=m2
```
## Cassandra Sink Config
For Confluent Kafka Cassandra connector, following properties file is used. 
apachelog-cassandra-sink.properties
```
name=apachelog-sink-orders
connector.class=com.datamountaineer.streamreactor.connect.cassandra.sink.CassandraSinkConnector
tasks.max=1
topics=cassTopic
connect.cassandra.export.route.query=INSERT INTO apachelog SELECT * FROM cassTopic
connect.cassandra.contact.points=localhost
connect.cassandra.port=9042
connect.cassandra.key.space=demo
connect.cassandra.username=cassandra
connect.cassandra.password=cassandra
```

## Create Cassandra Table
Create a table in Cassandra for storing the results.
```
create table apachelog (ipaddr varchar, clientid varchar, userid varchar, datetime varchar, method varchar, endpoint varchar, protocol  varchar, responseco de int, contentsize int, referrer varchar, useragent varchar);
```
## Running the Project
### Start Zookeeper
```
/usr/bin/zookeeper-server-start /etc/kafka/zookeeper.properties
```
### Start Kafka
```
/usr/bin/kafka-server-start /etc/kafka/server.properties
```
### Start Confluent Kafka-registry
```
/usr/bin/schema-registry-start /etc/schema-registry/schema-registry.properties
```
### Start Confluent Connect
```
/usr/bin/connect-distributed /etc/schema-registry/connect-avro-distributed.properties
```

### Post Cassandra config to the connect
```
java -jar kafka-connect-cli-0.5-all.jar create apachelog-sink-orders < apachelog-sink-distributed-orders.properties
```

### Start Flume
```
flume-ng agent -name flumePullAgent -c conf -f /home/pratyush04/flumeconf/flume-sparkpull.conf --classpath  /home/pratyush04/flume- plugins/spark-streaming-flume-sink_2.10-1.6.3.jar;/home/pratyush04/flume-plugins/spark-assembly-1.6.2-hadoop2.6.0.jar;/home/pratyush04/flume-plugins/scala-library-2.1 0.5.jar;/home/pratyush04/flume-plugins/commons-lang3-3.3.2.jar;/home/pratyush04/flume-plugins/avro-ipc-1.8.1.jar;/home/pratyush04/flume-plugins/avro-1.8.1.jar
```

### Run the jar file
```
spark-submit --class FlumePollingEventClient --master local[*] --conf "spark.driver.memory=500M" /home/pratyush04/LogReaderProject-assembly-1.0.jar  localhost 33349 localhost:9092
```

### Building the jar file
For building the jar file, sbt assembly was used
```
sbt assembly
```

### Apache Web Log Generator
For generating the continuous generating apache web logs, the script from Durga Gadiraju's github repo is used:
https://github.com/dgadiraju/code/tree/master/hadoop/edw/scripts/gen_logs

## References
* For checking more about Confluent's Kafka Cassandra connector, check out the following
https://www.confluent.io/blog/kafka-connect-cassandra-sink-the-perfect-match/

* ApacheAccessLog.java class from Databrick's Github repo is used for parsing web log lines
https://raw.githubusercontent.com/databricks/reference-apps/master/logs_analyzer/chapter1/java6/src/main/java/com/databricks/apps/logs/ApacheAccessLog.java
