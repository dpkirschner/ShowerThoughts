package com.dank.showerthoughts;

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
import com.dank.showerthoughts.services.RedditManager;

public class ShowerThoughtsSpeechlet implements Speechlet {
    private static final String ERROR_TEXT = "I'm sorry, but I can't process your request right now. Please try again later. Goodbye.";
	private static final String REPROMPT_TEXT = "Say 'another' to hear a new shower thought, or exit to quit";
	private static final String SUBREDDIT_NAME = "showerthoughts";
	private static final String CLOSE_TEXT = "Goodbye!";
	private static final String HELP_TEXT = "Ask Shower Thoughts to say something random!";
	private static final String CARD_TITLE = "Shower Thoughts";
	private static final Logger log = LoggerFactory.getLogger(ShowerThoughtsSpeechlet.class);
    private RedditManager reddit;
    
    public ShowerThoughtsSpeechlet(RedditManager reddit) {
    	super();
    	this.reddit = reddit;
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

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        SpeechletResponse response;
        switch (intentName) {
        case "ThoughtIntent":
        	response = getRepromptResponse();
        	break;
        case "AMAZON.HelpIntent":
        	response = ResponseFactory.askResponse(HELP_TEXT);
        	break;
        case "AMAZON.StopIntent":
        case "AMAZON.CancelIntent":
        	response = ResponseFactory.closeResponse(CLOSE_TEXT, CARD_TITLE);
        	break;
        default:
        	throw new SpeechletException(String.format("Could not recognize intent: '%s'", intentName));
        }
        logResponse(request, session, response);
        return response;
    }
      
    /**
     * Creates a {@code SpeechletResponse} for the command intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getRepromptResponse() {
    	SpeechletResponse response = null;
    	try{
    		StringBuilder speechTextBuilder = new StringBuilder();
    		speechTextBuilder.append(reddit.getRandomPostTitle(SUBREDDIT_NAME));
    		speechTextBuilder.append(". ");
    		speechTextBuilder.append(REPROMPT_TEXT);
            response = ResponseFactory.askResponse(speechTextBuilder.toString(), REPROMPT_TEXT, CARD_TITLE);
    	} catch (Exception e) {
            response = ResponseFactory.closeResponse(ERROR_TEXT, CARD_TITLE);
    	}
        
        return response;
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
