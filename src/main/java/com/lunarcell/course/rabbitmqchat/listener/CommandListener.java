package com.lunarcell.course.rabbitmqchat.listener;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.lunarcell.course.rabbitmqchat.dto.Command;

@Profile("server")
@Component
@RabbitListener(queues = "command", containerFactory = "retryExchangeContainerFactory", ackMode = "MANUAL")
public class CommandListener {
	
    @RabbitHandler
    public void receive(Command command) throws Exception {
        System.out.println(" [x] Received '" + command.getBody() + "'");
        throw new Exception("Exception occurred at command");
    }	
}
