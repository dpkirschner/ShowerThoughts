package com.dank.showerthoughts;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.oauth.OAuthException;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

public final class ShowerThoughtsSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
	private static final Set<String> supportedApplicationIds = new HashSet<String>();
	static {
	    supportedApplicationIds.add("amzn1.echo-sdk-ams.app.9c42a004-f578-4487-9ec4-586910988c06");
	}
	
	public ShowerThoughtsSpeechletRequestStreamHandler() throws NetworkException, OAuthException, IOException {
	    super(new ShowerThoughtsSpeechlet(), supportedApplicationIds);
	}
}
