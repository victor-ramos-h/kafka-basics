package com.vramosh.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class WordCounter {

    public static void main(String[] args){

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "word-counter-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();

        // 1->> Create a stream from a topic
        KStream<String, String> wordsInput = builder.stream("words-input");

        KTable<Windowed<String>, Long> wordsCount = wordsInput
                // 2->> Map values to lowercase
                .mapValues(value -> value.toLowerCase())
                // 3->> Flat map values
                .flatMapValues(value -> Arrays.asList(value.split(" ")))
                // 4->> Select a new key
                .selectKey((key, value) -> value)
                // 5->> Group key before aggregation
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofSeconds(10)))
                // 6->> Count words
                .count(Named.as("counts"));

        // 7->> Write the results back to Kafka
        wordsCount.toStream()
                .map((window, value) -> KeyValue.pair(window.key() ,value))
                .to("words-count", Produced.with(Serdes.String(),Serdes.Long()));

        KafkaStreams app = new KafkaStreams(builder.build(), props);
        app.start();
    }
}
