package com.wolf.raft.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.wolf.raft")
public class RaftApplication8081 {

	public static void main(String[] args) {

		SpringApplication.run(RaftApplication8081.class, args);
	}

}

