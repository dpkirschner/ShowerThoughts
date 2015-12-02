package com.dank.showerthoughts;

import java.io.IOException;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.oauth.OAuthException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.dank.echo.utils.ResponseFactory;
import com.dank.echo.utils.ResponseFactoryToken;
import com.dank.showerthoughts.services.RedditManager;

public class ShowerThoughtsSpeechlet implements Speechlet {
    protected static final String ERROR_TEXT = "I'm sorry, but I can't process your request right now. Please try again later. Goodbye.";
    protected static final String REPROMPT_TEXT = "Say 'another' to hear a new shower thought, or exit to quit";
    protected static final String SUBREDDIT_NAME = "showerthoughts";
    protected static final String CLOSE_TEXT = "Goodbye!";
    protected static final String HELP_TEXT = "Ask Shower Thoughts to say something random!";
    protected static final String CARD_TITLE = "Shower Thoughts";
    
	private static final Logger log = LoggerFactory.getLogger(ShowerThoughtsSpeechlet.class);
    private RedditManager reddit;
    
    public ShowerThoughtsSpeechlet() throws NetworkException, OAuthException, IOException {
    	super();
    	this.reddit = RedditManager.getInstance();
    }
    
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
    }

    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        return getRepromptResponse();
    }

    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
    }
    
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
              session.getSessionId());

        String intentName = parseIntentName(request);

        SpeechletResponse response;
        switch (intentName) {
        case "ThoughtIntent":
        	response = getRepromptResponse();
        	break;
        case "AMAZON.HelpIntent":
        	response = ResponseFactory.from(
        			ResponseFactoryToken.builder()
        				.outputSpeech(HELP_TEXT)
        				.repromptSpeech(HELP_TEXT)
        				.build()
        	);
        	break;
        case "AMAZON.StopIntent":
        case "AMAZON.CancelIntent":
        	response = ResponseFactory.from(
        			ResponseFactoryToken.builder()
        				.outputSpeech(CLOSE_TEXT)
        				.cardTitle(CARD_TITLE)
        				.cardContent(CLOSE_TEXT)
        				.shouldEndSession(true)
        				.build()
        	);
        	break;
        default:
        	throw new SpeechletException(String.format("Could not recognize intent: '%s'", intentName));
        }
        logResponse(request, session, response);
        return response;
    }

	/**
	 * This method returns the intent name if it exists, or throws a {@link SpeechletException}
	 * if the intent is malformed in some way.
	 * @param request
	 * @return a string representing the intentName
	 * @throws SpeechletException if the Intent is malformed
	 */
	private String parseIntentName(final IntentRequest request)
			throws SpeechletException {
		Intent intent = request.getIntent();
		if(intent == null) {
			throw new SpeechletException("Malformed Intent detected");
		}
		return intent.getName();
	}
      
    /**
     * Creates a {@code SpeechletResponse} for the command intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getRepromptResponse() {
    	SpeechletResponse response = null;
    	try{
    		String speechText = buildSpeechText();
    		
            response = ResponseFactory.from(
        			ResponseFactoryToken.builder()
        				.outputSpeech(speechText)
        				.repromptSpeech(REPROMPT_TEXT)
        				.cardTitle(CARD_TITLE)
        				.cardContent(speechText)
        				.build()
        	);
    	} catch (Exception e) {
    		response = ResponseFactory.from(
        			ResponseFactoryToken.builder()
        				.outputSpeech(ERROR_TEXT)
        				.cardTitle(CARD_TITLE)
        				.cardContent(ERROR_TEXT)
        				.shouldEndSession(true)
        				.build()
        	);
    	}
        
        return response;
    }

	/**
	 * Pulls the post title from Reddit and constructs the speech text.
	 * @return a string representing the 
	 */
	private String buildSpeechText() {
		StringBuilder speechTextBuilder = new StringBuilder();
		speechTextBuilder.append(reddit.getRandomPostTitle(SUBREDDIT_NAME));
		speechTextBuilder.append(". ");
		speechTextBuilder.append(REPROMPT_TEXT);
		return speechTextBuilder.toString();
	}
    
    private void logResponse(final IntentRequest request, final Session session, final SpeechletResponse response) {
    	log.info("onSessionEnded requestId={}, sessionId={}, speechText={}, responseText={}"
    			, request.getRequestId()
    			, session.getSessionId()
    			, response.getOutputSpeech()
    			, response.getReprompt() == null ? "no reprompt speech" : response.getReprompt().getOutputSpeech()
    			);
    }
}
