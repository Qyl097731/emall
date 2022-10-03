package com.nju.emall.order;

import com.nju.emall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.UUID;

@Slf4j
@SpringBootTest
class OrderApplicationTests {

    /**
     * 1.如何创建exchange queue binding
     * 使用AmqpAdmin进行创建
     * 2.如何手法消息
     */

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void createExchange() {
        //声明交换机
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("创建成功直接交换机");
    }

    @Test
    public void createQueue() {
        //声明队列
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("创建成功队列");
    }

    @Test
    public void createBinding() {
        //声明绑定
        Binding binding = new Binding("hello-java-queue", Binding.DestinationType.QUEUE,
                "hello-java-exchange", "hello.java", null);
        amqpAdmin.declareBinding(binding);
        log.info("创建成功绑定");
    }

    @Test
    public void sendMessageTest() {
        // 发送消息
        rabbitTemplate.convertAndSend("hello-java-exchange","hello.java","hello world",
                new CorrelationData(UUID.randomUUID().toString()));
        log.info("消息发送成功");


        // 如果发送的是一个对象 需要实现序列化
        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setCreateTime(new Date());
        reasonEntity.setId(1L);
        reasonEntity.setName("哈哈");
        rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",reasonEntity,new CorrelationData(UUID.randomUUID().toString()));
        log.info("消息发送成功 reasonEntity");
    }

    @Test
    void contextLoads() {
    }

}
