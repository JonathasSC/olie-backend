package com.olie.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.olie.api.notification.NotificationProperties;
import com.olie.api.security.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, NotificationProperties.class})
@EnableScheduling
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
