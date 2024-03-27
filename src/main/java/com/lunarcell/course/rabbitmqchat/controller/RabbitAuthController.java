package com.lunarcell.course.rabbitmqchat.controller;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunarcell.course.rabbitmqchat.dto.ResourcePathRequest;
import com.lunarcell.course.rabbitmqchat.dto.TopicPathRequest;
import com.lunarcell.course.rabbitmqchat.dto.UserPathRequest;
import com.lunarcell.course.rabbitmqchat.dto.VhostPathRequest;

@RestController
@RequestMapping("/rabbit/auth")
public class RabbitAuthController {

	@Autowired
	ObjectMapper objectMapper;

	@GetMapping
	public String index() {
		return "ok";
	}

	@PostMapping(path = "/user", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String postUser(UserPathRequest request) throws JsonProcessingException {

		System.out.println("postUser " + objectMapper.writeValueAsString(request));

		if ("user".equals(request.getUsername()) && "pass".equals(request.getPassword())) {
			return "allow administrator";
		} else {
			return "deny";
		}
	}

	@PostMapping("/vhost")
	public String postVhost(VhostPathRequest request) throws JsonProcessingException {

		System.out.println("postVhost " + objectMapper.writeValueAsString(request));

		if ("user".equals(request.getUsername()) && "chat".equals(request.getVhost())) {
			return "allow";
		} else {
			return "deny";
		}
	}

	@PostMapping("/resource")
	public String postResource(ResourcePathRequest request) throws JsonProcessingException {

		System.out.println("postResource " + objectMapper.writeValueAsString(request));

		if ("user".equals(request.getUsername())
				&& "chat".equals(request.getVhost())) {
			if ("exchange".equals(request.getResource())
					&& "request".equals(request.getName())
					&& Arrays.asList("configure", "write").stream().anyMatch(request.getPermission()::equals)) {
				return "allow";
			}
		}

		return "deny";
	}

	@PostMapping("/topic")
	public String postTopic(TopicPathRequest request) throws JsonProcessingException {

		System.out.println("postTopic " + objectMapper.writeValueAsString(request));

		Pattern pattern = Pattern.compile("^(chat|command)\\.\\w+");

		if ("user".equals(request.getUsername())
				&& "chat".equals(request.getVhost())
				&& "topic".equals(request.getResource())
				&& "request".equals(request.getName())
				&& "write".equals(request.getPermission())
				&& (request.getRouting_key() == null || pattern.matcher(request.getRouting_key()).find())) {
			return "allow";
		} else {
			return "deny";
		}
	}
}
