package com.jinn.ratejinn;

import com.jinn.ratejinn.config.RateJinnConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RateJinnConfig.class)
public class RatejinnApplication {

	public static void main(String[] args) {
		SpringApplication.run(RatejinnApplication.class, args);
	}

}
