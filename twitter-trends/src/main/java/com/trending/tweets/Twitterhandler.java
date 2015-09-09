package com.trending.tweets;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = { "com.trending.*" })
@PropertySource("classpath:twitter.properties")

public class Twitterhandler {
	public final Logger log = LoggerFactory.getLogger(Twitterhandler.class);
	
	//http://www.mkyong.com/spring/spring-propertysources-example/
	@Value("${ConsumerKey}")
	private String consumerKey;

	@Value("${ConsumerSecret}")
	private String consumerSecret;
	
	@Bean
	public String encodeKeys(){
		try {
			String encodedConsumerKey = URLEncoder.encode(consumerKey, "UTF-8");
			String encodedConsumerSecret = URLEncoder.encode(consumerSecret, "UTF-8");
	        String fullKey = encodedConsumerKey + ":" + encodedConsumerSecret;
	        log.info("Key to be encoded" + fullKey);
	        byte[] encodedBytes = Base64.encodeBase64(fullKey.getBytes());
	        return new String(encodedBytes);			
		}
		catch (UnsupportedEncodingException e) {
	        return new String();
	    }
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	public static void main(String[] args) {
		SpringApplication.run(Twitterhandler.class, args);
		Twitterhandler th = new Twitterhandler();
		th.encodeKeys();
	}

}
