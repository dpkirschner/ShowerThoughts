package com.dank.showerthoughts.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.Submission;

public class RedditManager {
	private static UserAgent myUserAgent = UserAgent.of("Echo", "com.dank.showerthoughts", "0.1", "dpkirschner");
	private static String fileName = "config.properties";
	
	private RedditClient redditClient;
	private String userName;
	private String password;
	private String clientId;
	private String clientSecret;
	
	public static RedditManager getInstance() throws NetworkException, OAuthException, IOException {
		return new RedditManager();
	}
	
	public RedditManager() throws NetworkException, OAuthException, IOException {
		redditClient = new RedditClient(myUserAgent);
		loadProperties();
		Credentials credentials = Credentials.script(userName, password, clientId, clientSecret);
		OAuthData authData = redditClient.getOAuthHelper().easyAuth(credentials);
		redditClient.authenticate(authData);
	}
	
	private void loadProperties() throws IOException {
		InputStream input = new FileInputStream(fileName);
		Properties prop = new Properties();
		prop.load(input);
		userName = prop.getProperty("username");
		password = prop.getProperty("password");
		clientId = prop.getProperty("clientId");
		clientSecret = prop.getProperty("clientSecret");
	}
	
	public String getRandomPostTitle(String subReddit) {
		Submission submission = redditClient.getRandomSubmission(subReddit);
		
		return submission.getTitle();
	}
}
