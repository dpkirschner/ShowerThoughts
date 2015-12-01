package com.dank.showerthoughts;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;

import java.io.IOException;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.oauth.OAuthException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.annotation.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.dank.showerthoughts.services.RedditManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RedditManager.class, SessionEndedRequest.class, SessionStartedRequest.class, Intent.class})
public class ShowerThoughtsSpeechletTest {	
	private static final String SESSION_ID = "NOT A REAL SESSION ID";
	private static final String REQUEST_ID = "NOT A REAL REQUEST ID";
	private static final String SAMPLE_TEXT = "SAMPLE REDDIT TEXT";
	
	@Mock
	private RedditManager mockReddit;
	@Mock
	private LaunchRequest mockLaunchRequest;
	@Mock
	private Session mockSession;
	@Mock
	private SessionStartedRequest mockSessionStartRequest;
	@Mock
	private SessionEndedRequest mockSessionEndRequest;
	@Mock
	private IntentRequest mockIntentRequest;
	@Mock
	private Intent mockIntent;
	
	private ShowerThoughtsSpeechlet speechlet;
	
	@Before
	public void setup() throws NetworkException, OAuthException, IOException {
		setRedditManagerExpectations(mockReddit);
		speechlet = new ShowerThoughtsSpeechlet(); 
	}
	
	/**
	 * Sample test to cover the onSessionStarted. Not currently useful
	 * @throws SpeechletException if there is an issue during the method call
	 */
	@Test
	public void onSessionStarted() throws SpeechletException {
		setSessionStartRequestExpectations();
		setSessionExpectations(1);
		speechlet.onSessionStarted(mockSessionStartRequest, mockSession);
	}
	
	/**
	 * Sample test to cover the onSessionEnded. Not currently useful
	 * @throws SpeechletException if there is an issue during the method call
	 */
	@Test
	public void onSessionEnded() throws SpeechletException {
		setSessionEndRequestExpectations();
		setSessionExpectations(1);
		speechlet.onSessionEnded(mockSessionEndRequest, mockSession);
	}
	
	/**
	 * Tests the case where the call to Reddit succeeds. In this case we expect
	 * a reprompt response.
	 * @throws SpeechletException
	 */
	@Test
	public void onLaunchHappyCase() throws SpeechletException {
		setLaunchRequestExpectations();
		setSessionExpectations(1);
		setSuccessfulRedditCallExpectations();
		
		SpeechletResponse response = speechlet.onLaunch(mockLaunchRequest, mockSession);

    	assertFalse(response.getShouldEndSession());
    	assertNotNull(response.getReprompt());
    	assertNotNull(response.getOutputSpeech());
    	assertNotNull(response.getCard());
	}
	
	/**
	 * Tests the case where the call to Reddit fails for some reason. In this case we do not
	 * expect a reprompt response.
	 * @throws SpeechletException
	 */
	@Test
	public void onLaunchFailure() throws SpeechletException {
		setLaunchRequestExpectations();
		setFailedRedditCallExpectations();
		
		SpeechletResponse response = speechlet.onLaunch(mockLaunchRequest, mockSession);
		
    	assertTrue(response.getShouldEndSession());
    	assertNull(response.getReprompt());
    	assertNotNull(response.getOutputSpeech());
    	assertNotNull(response.getCard());
	}
	
	/**
	 * Tests the case where the intent is malformed and we can not determine which intent
	 * the user wanted
	 * @throws SpeechletException
	 */ 
	@Test(expected=SpeechletException.class)
	public void onNullIntent() throws SpeechletException {
		setSessionExpectations(1);
		setIntentRequestExpectations(null);
		speechlet.onIntent(mockIntentRequest, mockSession);
	}
	 
	@Test
	public void onThoughtIntent() throws SpeechletException {
		setIntentRequestExpectations(mockIntent);
		setIntentNameExpectations("ThoughtIntent");
		setSessionExpectations(2);
		setSuccessfulRedditCallExpectations();
		
		SpeechletResponse response = speechlet.onIntent(mockIntentRequest, mockSession);
		
    	assertFalse(response.getShouldEndSession());
    	assertNotNull(response.getReprompt());
    	assertNotNull(response.getOutputSpeech());
    	assertNotNull(response.getCard());
	}
	
	@Test
	public void onAmazonHelpIntent() throws SpeechletException {
		setIntentRequestExpectations(mockIntent);
		setIntentNameExpectations("AMAZON.HelpIntent");
		setSessionExpectations(2);
		setSuccessfulRedditCallExpectations();
		
		SpeechletResponse response = speechlet.onIntent(mockIntentRequest, mockSession);
		
    	assertFalse(response.getShouldEndSession());
    	assertNotNull(response.getReprompt());
    	assertNotNull(response.getOutputSpeech());
    	assertNull(response.getCard());
	}
	
	@Test
	public void onAmazonStopIntent() throws SpeechletException {
		setIntentRequestExpectations(mockIntent);
		setIntentNameExpectations("AMAZON.StopIntent");
		setSessionExpectations(2);
		setSuccessfulRedditCallExpectations();
		
		SpeechletResponse response = speechlet.onIntent(mockIntentRequest, mockSession);
		
    	assertTrue(response.getShouldEndSession());
    	assertNull(response.getReprompt());
    	assertNotNull(response.getOutputSpeech());
    	assertNotNull(response.getCard());
	}
	
	@Test
	public void onAmazonCancelIntent() throws SpeechletException {
		setIntentRequestExpectations(mockIntent);
		setIntentNameExpectations("AMAZON.CancelIntent");
		setSessionExpectations(2);
		setSuccessfulRedditCallExpectations();
		SpeechletResponse response = speechlet.onIntent(mockIntentRequest, mockSession);
		
    	assertTrue(response.getShouldEndSession());
    	assertNull(response.getReprompt());
    	assertNotNull(response.getOutputSpeech());
    	assertNotNull(response.getCard());
	}

	private void setSessionEndRequestExpectations() {
		expect(mockSessionEndRequest.getRequestId()).andReturn(REQUEST_ID);
		replay(mockSessionEndRequest);
	}

	private void setSessionStartRequestExpectations() {
		expect(mockSessionStartRequest.getRequestId()).andReturn(REQUEST_ID);
		replay(mockSessionStartRequest);
	}

	private void setLaunchRequestExpectations() {
		expect(mockLaunchRequest.getRequestId()).andReturn(REQUEST_ID);
		replay(mockLaunchRequest);
	}
	
	private void setRedditManagerExpectations(RedditManager redditManager) throws OAuthException, IOException {
		mockStatic(RedditManager.class);
		expect(RedditManager.getInstance()).andReturn(redditManager);
		replay(RedditManager.class);
	}

	private void setIntentRequestExpectations(Intent intent) {
		expect(mockIntentRequest.getRequestId()).andReturn(REQUEST_ID).times(2);
		expect(mockIntentRequest.getIntent()).andReturn(intent);
		replay(mockIntentRequest);
	}

	private void setIntentNameExpectations(String intentName) {
		expect(mockIntent.getName()).andReturn(intentName);
		replay(mockIntent);
	}

	private void setSuccessfulRedditCallExpectations() {
		expect(mockReddit.getRandomPostTitle(ShowerThoughtsSpeechlet.SUBREDDIT_NAME)).andReturn(SAMPLE_TEXT);
		replay(mockReddit);
	}

	private void setFailedRedditCallExpectations() {
		expect(mockReddit.getRandomPostTitle(ShowerThoughtsSpeechlet.SUBREDDIT_NAME))
			.andThrow(new RuntimeException("OH NO! Something went wrong"));
		replay(mockReddit);
	}

	private void setSessionExpectations(int count) {
		expect(mockSession.getSessionId()).andReturn(SESSION_ID).times(count);
		replay(mockSession);
	}
}
