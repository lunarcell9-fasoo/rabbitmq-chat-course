package com.lunarcell.course.rabbitmqchat.config;

import java.util.ArrayList;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.lunarcell.course.rabbitmqchat.error.RetryExchange;
import com.lunarcell.course.rabbitmqchat.error.RetryExchangeInterceptor;

@Profile("server")
@Configuration
public class RabbitConfig {

	final int MAX_INTERVAL = 10000;

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
	public CustomExchange chatHash() {
		return new CustomExchange("chat-hash", "x-consistent-hash");
	}

	@Bean
	public Declarables chatHashQueueAndBindings(@Value("${rabbitmq.server.chat-concurrent}") int concurrent,
			CustomExchange chatHash) {
		List<Declarable> declarables = new ArrayList<>(concurrent * 2);

		for (int i = 0; i < concurrent; i++) {
			Queue queue = new Queue("chat." + i);
			declarables.add(queue);
			declarables.add(new Binding(queue.getName(), DestinationType.QUEUE, chatHash.getName(), "1", null));
		}

		return new Declarables(declarables);
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
	public Binding bindingRequestToChatHash(TopicExchange request, CustomExchange chatHash) {
		return BindingBuilder.bind(chatHash).to(request).with("chat.#");
	}

	@Bean
	public Binding bindingChatToUser(TopicExchange chat, TopicExchange user) {
		return BindingBuilder.bind(user).to(chat).with("*.user.#");
	}



	/// retry


	@Bean
	public RetryExchange retryExchange(FanoutExchange commandRetryExchange) {
		return new RetryExchange(1000, 3, MAX_INTERVAL, 5, commandRetryExchange);
	}
	
	@Bean
    public RetryExchangeInterceptor retryExchangeInterceptor(RabbitTemplate rabbitTemplate, RetryExchange retryExchange) {
        return new RetryExchangeInterceptor(rabbitTemplate, retryExchange);
    }

	@Bean
	public SimpleRabbitListenerContainerFactory retryExchangeContainerFactory(ConnectionFactory connectionFactory,
			RetryExchangeInterceptor retryInterceptor) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);

		Advice[] adviceChain = { retryInterceptor };
		factory.setAdviceChain(adviceChain);

		return factory;
	}

	@Bean
	public FanoutExchange commandRetryExchange() {
		return new FanoutExchange("command.retry");
	}

	@Bean
	public Queue commandRetryQueue() {
		return QueueBuilder.durable("command.retry").ttl(MAX_INTERVAL).deadLetterExchange("request").build();
	}

	@Bean
	public Binding bindingCommandRetry(FanoutExchange commandRetryExchange, Queue commandRetryQueue) {
		return BindingBuilder.bind(commandRetryQueue).to(commandRetryExchange);
	}
}
