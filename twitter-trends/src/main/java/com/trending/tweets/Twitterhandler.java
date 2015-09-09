package com.trending.tweets;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class Twitterhandler {
	public final Logger log = LoggerFactory.getLogger(Twitterhandler.class);
	
	//http://www.mkyong.com/spring/spring-propertysources-example/
	@Value("${ConsumerKey}")
	private String consumerKey;

	@Value("${ConsumerSecret}")
	private String consumerSecret;
	
	@RequestMapping("/getTrend")
	public String getTrendingTweets(@RequestParam(value = "name", required = false, defaultValue = "World") String name, Model model) {
		String encodedKeys = encodeKeys();
		System.out.println("***************"+encodedKeys);
		model.addAttribute("name", name);
		return "index";
	}
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
}
