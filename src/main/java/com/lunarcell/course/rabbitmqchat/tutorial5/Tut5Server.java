package com.lunarcell.course.rabbitmqchat.tutorial5;

import org.springframework.amqp.rabbit.annotation.RabbitListener;

public class Tut5Server {

	@RabbitListener(queues = "tut.rpc.requests")
	// @SendTo("tut.rpc.replies") used when the client doesn't set replyTo.
	public long fibonacci(long n) {
		System.out.println(" [x] Received request for " + n);
		long result = fib(n);
		System.out.println(" [.] Returned " + result);
		return result;
	}

	public long fib(long n) {
		return n == 0 ? 0 : n == 1 ? 1 : (fib(n - 1) + fib(n - 2));
	}

}