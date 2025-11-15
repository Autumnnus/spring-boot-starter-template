package com.autumnus.spring_boot_starter_template.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQConfig {

    private final NotificationMessagingProperties properties;

    public RabbitMQConfig(NotificationMessagingProperties properties) {
        this.properties = properties;
    }

    @Bean
    public TopicExchange notificationsExchange() {
        return new TopicExchange(properties.getExchange(), true, false);
    }

    @Bean
    public Queue notificationsQueue() {
        return new Queue(properties.getQueue(), true);
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, TopicExchange notificationsExchange) {
        return BindingBuilder.bind(notificationsQueue)
                .to(notificationsExchange)
                .with(properties.getRoutingKey());
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jacksonMessageConverter) {
        final RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter);
        return template;
    }
}
