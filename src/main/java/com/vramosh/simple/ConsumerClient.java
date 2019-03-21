package com.vramosh.simple;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConsumerClient implements Runnable{

    private KafkaConsumer<String,String> consumer;
    
    public void init(){
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-consumer");
        
        consumer = new KafkaConsumer<>(props);
        
    }
    
    public void run(){
        System.out.println("Starting consumer. Thread.name="+Thread.currentThread().getName());
        
        try{
            consumer.subscribe(Arrays.asList("topic-name"));
             
            while(true){
                ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println( 
                            String.format("Thread %s received key=%s value=%s", 
                                    Thread.currentThread().getName(), 
                                    record.key(), 
                                    record.value())
                    );
                }
            }
        } catch (Exception e){
            
        } finally {
            consumer.close();
        }
    }
    
    public void shutdown(){
        consumer.wakeup();
    }
    
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        System.out.println("Press enter to start consumer1");
        sc.nextLine();
        ConsumerClient consumer1 = new ConsumerClient();
        consumer1.init();
        executor.submit(consumer1);

        System.out.println("Press enter to start consumer2");
        sc.nextLine();
        ConsumerClient consumer2 = new ConsumerClient();
        consumer2.init();
        executor.submit(consumer2);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                consumer1.shutdown();
                consumer2.shutdown();
                executor.shutdown();
                sc.close();
                try {
                    executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    
}
