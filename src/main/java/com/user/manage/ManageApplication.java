package com.user.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import com.user.manage.config.JwtConfig;

@SpringBootApplication
@EnableConfigurationProperties(JwtConfig.class)
@EnableMethodSecurity
@EnableJpaAuditing
public class ManageApplication {

	public static void main(String[] args) {
		SpringApplication.run(ManageApplication.class, args);
	}

}
