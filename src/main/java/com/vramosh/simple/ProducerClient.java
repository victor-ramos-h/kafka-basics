package com.vramosh.simple;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Scanner;

public class ProducerClient {

    private KafkaProducer producer;

    public static void main(String[] args){
        ProducerClient app = new ProducerClient();
        app.init();
        app.run();
    }

    public void init(){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Are you using docker? Use 9094
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        producer = new KafkaProducer(props);
    }
    
    public void run(){
        System.out.println("Write messages: (0 to exit)");
        Scanner sc = new Scanner(System.in);
        do {
            String value = sc.nextLine();
            if(value.equals("0")) break;
            String key = value.hashCode() + "";
            ProducerRecord<String,String> record = new ProducerRecord<>("topic-name", key, value);
            producer.send(record);
        } while(true);
    }
}
