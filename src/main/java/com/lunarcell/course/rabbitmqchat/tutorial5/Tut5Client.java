package com.lunarcell.course.rabbitmqchat.tutorial5;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

public class Tut5Client {

	@Autowired
	private RabbitTemplate template;

	@Autowired
	private DirectExchange exchange;

	int start = 0;

	@Scheduled(fixedDelay = 1000, initialDelay = 500)
	public void send() {
		System.out.println(" [x] Requesting fib(" + start + ")");
		Object result = template.convertSendAndReceive(exchange.getName(), "rpc", start++);
		Long response = result instanceof Integer ? Long.valueOf((Integer) result) : (Long) result;
		System.out.println(" [.] Got '" + response + "'");
	}
}