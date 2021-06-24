package com.vramosh.streams;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class AnomalyGenerator {

    private KafkaProducer producer;

    public static void main(String[] args){
        AnomalyGenerator app = new AnomalyGenerator();
        app.init(args);
        app.run();
    }

    public void init(String[] args){
        final String bootstrapServers = args.length > 0 ? args[0] : "localhost:9092";
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        producer = new KafkaProducer(props);
    }

    public void run(){
        System.out.println("Logging in...");
        String users[] = new String[] { "vramos", "cfernandez", "hloza", "grivera", "cvertiz", "rhuanca", "mcallejas", "mjmichel", "rloza", "jrocha", "valaro", "orivera", "vsoliz", "epari", "avillalba", "rchirinos", "crengel", "avasquez", "arodriguez", "scalle", "dzuleta", "rflores", "nalarcon" };

        while(true){
            try {
                ProducerRecord<String,String> record = new ProducerRecord<>("loggins", users[getRandomNumber(0,22)]);
                producer.send(record);
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
}
