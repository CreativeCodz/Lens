package com.trending.tweets;

	
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@SpringBootApplication
@PropertySource("classpath:twitter.properties")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    @Bean
	public PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}
