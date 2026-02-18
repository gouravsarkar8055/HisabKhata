package com.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.user.repository")
public class HisabKhataApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(HisabKhataApplication.class, args);
	}

}
