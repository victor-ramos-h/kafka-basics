package com.vramosh.streams;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;

public class MyStreamApp {
    
    public static void main(String[] args){
        MyStreamApp app = new MyStreamApp();
        app.run();
    }
    
    public void run(){
        Serde<String> stringSerde = Serdes.String();
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, String> messages = builder.stream("topic-name", Consumed.with(stringSerde, stringSerde));

        KTable<String, Long> wordCount = messages
                .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
                .groupBy((key, value) -> value)
                .count();
        
        wordCount.toStream()
                .filter((key, value) -> value > 1000)
                .mapValues(value-> value+"")
                .to("wordcount", Produced.with(stringSerde,stringSerde));

        Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "my-stream-app");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, stringSerde.getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, stringSerde.getClass().getName());
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 4);
        
        KafkaStreams stream = new KafkaStreams( builder.build(), props );
        stream.start();
    }
}
