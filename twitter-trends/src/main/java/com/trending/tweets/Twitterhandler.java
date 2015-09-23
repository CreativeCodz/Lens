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
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import twitter4j.JSONException;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class Twitterhandler {
	public final Logger log = LoggerFactory.getLogger(Twitterhandler.class);
	
	@Value("${ConsumerKey}")
	private String consumerKey;

	@Value("${ConsumerSecret}")
	private String consumerSecret;
	private String bearerToken;
	
	@RequestMapping(value ="/getTrend", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE )
	public @ResponseBody String getTrendingTweets() throws JSONException, ParseException {
		bearerToken = requestBearerToken("https://api.twitter.com/oauth2/token");
		JSONArray trends = null;
		String trend = null;
		try {
			trends = fetchTrendingTweets("https://api.twitter.com/1.1/trends/place.json?id=1");
			ObjectMapper mapper = new ObjectMapper();
			mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
			trend = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(trends);
			log.info("JSONArray :" + trends);

			twitter4j.JSONObject jsonObject = new twitter4j.JSONObject(trends.get(0).toString());
			twitter4j.JSONArray trendsArray = jsonObject.getJSONArray("trends");
			String hashTag = null;
			for (int i = 0; i < trendsArray.length(); i++) {
				hashTag = trendsArray.getJSONObject(i).getString("name");
				log.info("HashTag : " + hashTag);
			}
			searchTweet(hashTag, bearerToken);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return trend;
	}
	 
	/**
	 * @param hashTag
	 * @throws IOException 
	 */
	private void searchTweet(String hashTag, String bearerToken) throws IOException {
		
		HttpsURLConnection connection = null;
		
		try {
			String endPointUrl = "https://api.twitter.com/1.1/search/tweets.json?count=100&result_type=recent&q=%23ScreamQueens";
			log.info(endPointUrl);
			URL url = new URL(endPointUrl ); 
			connection = (HttpsURLConnection) url.openConnection();           
			connection.setDoOutput(true);
			connection.setDoInput(true); 
			connection.setRequestMethod("GET"); 
			connection.setRequestProperty("Host", "api.twitter.com");
			connection.setRequestProperty("User-Agent", "CreativeCodz");
			connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
			connection.setUseCaches(false);
				
			Object obj = JSONValue.parse(readResponse(connection));
			
			log.info("Search hashtag tweet : "+ obj.toString());
		}
		catch (MalformedURLException e) {
			throw new IOException("Invalid endpoint URL specified.", e);
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		
		
	}

	/**
	 * @param endPointUrl
	 * @return
	 */
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
		JSONObject obj = (JSONObject)JSONValue.parse(readResponse(connection));
		log.info("Request Bearer Token : " + obj.toJSONString());
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
	
	/**
	 * @param connection
	 * @param textBody
	 * @return
	 */
	private boolean writeRequest(HttpsURLConnection connection, String textBody) {
		try {
			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			wr.write(textBody);
			wr.flush();
			wr.close();				
			return true;
		}
		catch (IOException e) { 
			return false; 
		}
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
	
	/**
	 * @return
	 */
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
	/**
	 * @param endPointUrl
	 * @return
	 * @throws IOException
	 */
	private JSONArray fetchTrendingTweets(String endPointUrl) throws IOException {
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
			
			log.info("JsonString is "+ obj.toJSONString());
			if (obj != null) {
				return obj;
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
