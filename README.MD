Kafka Basics
============

#### 0. Requirements

- Java 8
- Zookeeper 3.4.10+, Kafka 2.1.1 - Scala 2.11 [Download](https://www.apache.org/dyn/closer.cgi?path=/kafka/2.1.1/kafka_2.11-2.1.1.tgz)
- Gradle 3.5+

#### 1. Start Zookeeper and Kafka
   
   ``cd /path/to/your/kafka/folder/``

   ``bin/zookeeper-server-start.sh config/zookeeper.properties``

   ``bin/kafka-server-start.sh config/server.properties``

##### To stop them

   ``bin/kafka-server-stop.sh``

   ``bin/zookeeper-server-stop.sh``

##### Or using docker

- docker-compose.yml and .env files are provided in this git.

- Change MY_HOST_IP_OR_DNS value to your IP or DNS in the .env file.

~~~~
kafka:
      image: wurstmeister/kafka
      hostname: kafka
      environment:
        KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
        KAFKA_ADVERTISED_LISTENERS: INSIDE://:9092,OUTSIDE://${MY_HOST_IP_OR_DNS}:9094
        KAFKA_LISTENERS: INSIDE://:9092,OUTSIDE://:9094
        KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT
        KAFKA_INTER_BROKER_LISTENER_NAME: INSIDE
      ports:
        - 9094:9094
      volumes:
        - ./data/test1/kafka:/data
        - /var/run/docker.sock:/var/run/docker.sock
      
zookeeper:
    image: zookeeper:3.4.12
    ports:
      - 2181:2181
    volumes:
      - /data/zookeeper/data:/data
      - /data/zookeeper/datalog:/datalog
~~~~

#### 2. Create Topics

~~~~
bin/kafka-topics \
--zookeeper localhost:2181 \
--create \
--topic topic-name \
--partitions 2 \
--replication-factor 1
~~~~

#### 3. Console Producer and Consumer

   1. Start a simple consumer

   ~~~~
   bin/kafka-console-consumer \
   --bootstrap-server localhost:9092 \
   --from-beginning \
   --topic topic-name
   ~~~~

   2. Start a simple producer
   ~~~~
   bin/kafka-console-producer \
   --broker-list localhost:9092 \
   --topic topic-name
   ~~~~

#### 4. Write Producer and Consumer in Java

   1. Using Intellij IDEA or any IDE you prefer, create a java/gradle based project or clone the following git:

      ``https://github.com/victor-ramos-h/kafka-basics.git``

   2. Add the next dependencies to your build.gradle
      ~~~~
      compile group: 'org.apache.kafka', name: 'kafka-clients', version: '2.1.1'
      compile group: 'org.apache.kafka', name: 'kafka-streams', version: '2.1.1'
      ~~~~~

      The library _kafka-clients_ is enough to write simple producer and consumer and _kafka-streams_ is to write kafka streams based applications.
   
   3. Create a class ProducerClient with the next code: [ProducerClient.java](https://github.com/victor-ramos-h/kafka-basics/blob/master/src/main/java/com/vramosh/simple/ProducerClient.java)

   4. Create a class ConsumerClient with the next code: [ConsumerClient.java](https://github.com/victor-ramos-h/kafka-basics/blob/master/src/main/java/com/vramosh/simple/ConsumerClient.java)   

#### 5. Write a Kafka-Streams application

   1. Create a class MyStreamApp with the next code: [MyStreamApp.java]()
   
   2. Send many messages using a console producer. (A [random.txt]() file is provided)
   
      ``bin/kafka-console-producer \
           --broker-list localhost:9092 \
           --topic topic-name < /path/to/your/file/random.txt``
           
           
#### 6. Some other useful commands
    
   Get the list of topics
    
    ``bin/kafka-topics.sh --list --zookeeper localhost:2181``
   
   Get the configuration of a topic
   
   ``bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic topic-name``
   
   Alter a topic
   
   ``bin/kafka-topics.sh --zookeeper localhost:2181 --alter --topic topic-name --partitions 16``
   
   Delete a topic
   
   ``bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic topic-name``
   
   Produce a key-value record
   
   ``echo "key1,value1" | bin/kafka-console-producer.sh --broker-list localhost:9092 --property parse.key=true --property key.separator=, --topic topic-name``
   
   Consume messages and print key + value
   
   ``bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic topic-name --property print.key=true --property key.separator="," --from-beginning``
   
   Count messages in a topic 
   
   ``bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic topic-name --time -1 |awk -F  ":" '{sum += $3} END {print sum}'``
   
   List consumer groups
   
   ``bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list``
   
   Describe a consumer group (View the lag)
   
   ``bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group groupid``
   
   Reset offsets in a consumer group
   
   ``bin/kafka-consumer-group.sh --bootstrap-server localhost:9092 --group groupid --topic topic-name --reset-offsets --to-latest``
   
   ``bin/kafka-consumer-group.sh --bootstrap-server localhost:9092 --group groupid --topic topic-name --reset-offsets --to-latest --execute``
   