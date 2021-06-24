package com.vramosh.streams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AnomalyDetection {

    public static void main(final String[] args) {

        final Serde<String> stringSerde = Serdes.String();
        final StreamsBuilder builder = new StreamsBuilder();

        // INPUT
        final KStream<String, String> loggins = builder.stream("loggins");

        // COUNT LOGIN BY USER
        final KTable<Windowed<String>, Long> anomalousUsers = loggins
                .map((ignoredKey, username) -> new KeyValue<>(username, username))
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofSeconds(10)))
                .count()
                .filter((windowedUserId, count) -> count >= 3);

        // CLEAN PREVIOUS WINDOW AND PREPARE THE DATA
        final KStream<String, String> anomalousUsersForConsole = anomalousUsers
                .toStream()
                .filter((windowedUser, count) -> count != null)
                .map((windowedUser, count) -> toJson(windowedUser, count));

        // SEND TO TOPIC
        anomalousUsersForConsole.to("anomalous_users", Produced.with(stringSerde, stringSerde));

        final KafkaStreams streams = new KafkaStreams(builder.build(), configs(args));
        streams.cleanUp();
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private static Properties configs(String[] args) {
        final String bootstrapServers = args.length > 0 ? args[0] : "localhost:9094";
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "anomaly-detection");
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "anomaly-detection-client");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 500);
        return props;
    }

    // Not important for the purposes of this example.
    private static ObjectMapper mapper = new ObjectMapper();
    private static KeyValue<String, String> toJson(Windowed<String> windowedUser, Long count) {
        Map<String,String> resp = new HashMap<>();
        resp.put("user", windowedUser.key());
        resp.put("count", count.toString());
        String value = "";
        try {
            value = mapper.writeValueAsString(resp);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return KeyValue.pair(windowedUser.key(), value);
    }

}