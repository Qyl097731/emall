package com.nju.emall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @description
 * @date:2022/10/3 21:02
 * @author: qyl
 */
@Configuration
public class MyRabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制RabbitTemplate
     * MyRabbitConfig对象创建完成之后 执行这个方法
     *
     * 消费端确认(保证每个消息都被正确消费，此时才可以broker删除这个消息)
     *  1、默认自动确认，只要消息接收到，客户端自动确认，服务器就会移除这个消息，
     *      但是当收到很多消息后，会自动回复ACK，但如果只有一个消息处理成功就宕机，就会发生消息丢失
     *  2、手动确认，只要没有明确告诉MQ被接收，没有ACK，消息就一直都是Unacked
     *      即使Consumer宕机，消息不会丢失，会重新变为Ready，下一次新的Consumer链接进来就发给它
     *
     */
    @PostConstruct
    public void initRabbitTemplate() {
        // 设置自定义的确认回调
         /**
         * 1、只要消息抵达Broker就ack=true
         * correlationData：当前消息的唯一关联数据(这个是消息的唯一id)
         * ack：消息是否成功收到
         * cause：失败的原因
         */
        //设置确认回调
        rabbitTemplate.setConfirmCallback((correlationData,ack,cause) -> {
            System.out.println("confirm...correlationData["+correlationData+"]==>ack:["+ack+"]==>cause:["+cause+"]");
        });
        // 只要消息没有投递到队列。就会出发这个失败回调
        /**
         * 只要消息没有投递给指定的队列，就触发这个失败回调
         * message：投递失败的消息详细信息
         * replyCode：回复的状态码
         * replyText：回复的文本内容
         * exchange：当时这个消息发给哪个交换机
         * routingKey：当时这个消息用哪个路邮键
         */
        rabbitTemplate.setReturnCallback((message,replyCode,replyText,exchange,routingKey) -> {
            System.out.println("Fail Message["+message+"]==>replyCode["+replyCode+"]" +
                    "==>replyText["+replyText+"]==>exchange["+exchange+"]==>routingKey["+routingKey+"]");
        });
    }
}
