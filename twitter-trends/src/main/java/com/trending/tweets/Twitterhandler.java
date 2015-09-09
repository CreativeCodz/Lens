package com.trending.tweets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
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
	private String bearerToken;
	
	@RequestMapping("/getTrend")
	public String getTrendingTweets(@RequestParam(value = "name", required = false, defaultValue = "World") String name, Model model) {
		bearerToken = requestBearerToken("https://api.twitter.com/oauth2/token");
		try {
			fetchTrendingTweets("https://api.twitter.com/1.1/trends/place.json?id=1");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.addAttribute("name", name);
		return "index";
	}
	public String  requestBearerToken(String endPointUrl) {
		HttpsURLConnection connection = null;
		String encodedCredentials = encodeKeys();
		try {
		URL url = new URL(endPointUrl); 
		connection = (HttpsURLConnection) url.openConnection(); 
		connection.setDoOutput(true);
		connection.setDoInput(true); 
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Host", "api.twitter.com");
		connection.setRequestProperty("User-Agent", "CreativeCodz");
		connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"); 
		connection.setRequestProperty("Content-Length", "29");
		connection.setUseCaches(false);
		writeRequest(connection, "grant_type=client_credentials");
		// Parse the JSON response into a JSON mapped object to fetch fields from.
				JSONObject obj = (JSONObject)JSONValue.parse(readResponse(connection));
					System.out.println("**********************"+obj.toString());
				if (obj != null) {
					String tokenType = (String)obj.get("token_type");
					String token = (String)obj.get("access_token");
				
					return ((tokenType.equals("bearer")) && (token != null)) ? token : "";
				}
			}catch (IOException ioe) {
				//throw new IOException("Invalid endpoint URL specified.", ioe);
			}
			finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		return null;
	}
	// Writes a request to a connection
	private boolean writeRequest(HttpsURLConnection connection, String textBody) {
		try {
			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			wr.write(textBody);
			wr.flush();
			wr.close();
				
			return true;
		}
		catch (IOException e) { return false; }
	}
		
		
	// Reads a response for a given connection and returns it as a string.
	private String readResponse(HttpsURLConnection connection) {
		try {
			StringBuilder str = new StringBuilder();
				
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = "";
			while((line = br.readLine()) != null) {
				str.append(line + System.getProperty("line.separator"));
			}
			System.out.println("string"+str.toString());
			return str.toString();
		}
		catch (IOException e) { return new String(); }
	}
	public String encodeKeys() {
		try {
			String encodedConsumerKey = URLEncoder.encode(consumerKey, "UTF-8");
			String encodedConsumerSecret = URLEncoder.encode(consumerSecret, "UTF-8");
			String fullKey = encodedConsumerKey + ":" + encodedConsumerSecret;
			byte[] encodedBytes = Base64.encodeBase64(fullKey.getBytes());
			return new String(encodedBytes);
		} catch (UnsupportedEncodingException e) {
			return new String();
		}

	}
	// Fetches the first tweet from a given user's timeline
	private String fetchTrendingTweets(String endPointUrl) throws IOException {
		HttpsURLConnection connection = null;
					
		try {
			URL url = new URL(endPointUrl); 
			connection = (HttpsURLConnection) url.openConnection();           
			connection.setDoOutput(true);
			connection.setDoInput(true); 
			connection.setRequestMethod("GET"); 
			connection.setRequestProperty("Host", "api.twitter.com");
			connection.setRequestProperty("User-Agent", "CreativeCodz");
			connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
			connection.setUseCaches(false);
				
				
			// Parse the JSON response into a JSON mapped object to fetch fields from.
			JSONArray obj = (JSONArray)JSONValue.parse(readResponse(connection));
				System.out.println("*******************"+obj.toString());
			if (obj != null) {
				String tweet = ((JSONObject)obj.get(0)).get("trends").toString();

				return (tweet != null) ? tweet : "";
			}
		}
		catch (MalformedURLException e) {
			throw new IOException("Invalid endpoint URL specified.", e);
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return null;
	}
}
