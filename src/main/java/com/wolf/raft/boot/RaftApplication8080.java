package com.wolf.raft.boot;

import com.wolf.raft.Container;
import com.wolf.raft.Initializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.wolf.raft")
public class RaftApplication8080 {

	public static void main(String[] args) {

		SpringApplication.run(RaftApplication8080.class, args);

		Initializer initializer = Container.getBean("initializer",Initializer.class);
		initializer.init();
	}

}

