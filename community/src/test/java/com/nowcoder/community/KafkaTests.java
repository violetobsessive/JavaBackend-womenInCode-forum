package com.nowcoder.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)

public class KafkaTests {
    @Autowired
    private MyKafkaProducer kafkaProducer;

    @Test
    public void testKafka() {
        kafkaProducer.sendMessage("test", "你好");
        kafkaProducer.sendMessage("test", "在吗");

        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
    @Component
    class MyKafkaProducer {

        @Autowired
        private KafkaTemplate kafkaTemplate;

        //生产者主动调用消息，随时
        public void sendMessage(String topic, String content) {
            kafkaTemplate.send(topic, content);
        }

    }

    @Component
    class KafkaConsumer {

        @KafkaListener(topics = {"test"})
        //把消息封装在ConsumerRecord，被动处理消息，有延时
        public void handleMessage(ConsumerRecord record) {
            System.out.println(record.value());
        }
    }

