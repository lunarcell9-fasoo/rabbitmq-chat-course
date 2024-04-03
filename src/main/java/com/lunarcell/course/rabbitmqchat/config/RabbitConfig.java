package com.lunarcell.course.rabbitmqchat.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("server")
@Configuration
public class RabbitConfig {

	@Bean
	public RabbitAdmin amqpAdmin(ConnectionFactory connectionFactory) {
		return new RabbitAdmin(connectionFactory);
	}

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setPrefetchCount(1);
		factory.setConcurrentConsumers(1);
		factory.setMaxConcurrentConsumers(10);

		return factory;
	}

	@Bean
	public Queue deadLetterQueue() {
		return new Queue("dead-letter");
	}

	@Bean
	public TopicExchange request() {
		return new TopicExchange("request");
	}

	@Bean
	public Queue command() {
		return new Queue("command");
	}

	@Bean
	public TopicExchange chat() {
		return new TopicExchange("chat");
	}

	@Bean
	public TopicExchange user() {
		return new TopicExchange("user");
	}

	@Bean
	public Binding bindingRequestToCommand(TopicExchange request, Queue command) {
		return BindingBuilder.bind(command).to(request).with("command.#");
	}

	@Bean
	public Binding bindingRequestToChat(TopicExchange request, TopicExchange chat) {
		return BindingBuilder.bind(chat).to(request).with("chat.#");
	}

	@Bean
	public Binding bindingChatToUser(TopicExchange chat, TopicExchange user) {
		return BindingBuilder.bind(user).to(chat).with("*.user.#");
	}
}
