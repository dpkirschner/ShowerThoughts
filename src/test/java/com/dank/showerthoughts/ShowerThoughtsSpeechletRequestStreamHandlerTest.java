package com.dank.showerthoughts;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;

import java.io.IOException;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.oauth.OAuthException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.dank.showerthoughts.services.RedditManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RedditManager.class)
public class ShowerThoughtsSpeechletRequestStreamHandlerTest {
	
	@Before
	public void setup() {
		mockStatic(RedditManager.class);
	}
	
	@Test
	public void happyUseCase() throws NetworkException, OAuthException, IOException {
		expect(RedditManager.getInstance()).andReturn(null);
		ShowerThoughtsSpeechletRequestStreamHandler streamHandler = new ShowerThoughtsSpeechletRequestStreamHandler();
		assertNotNull(streamHandler);
	}
	
	@Test(expected=OAuthException.class)
	public void oauthException() throws NetworkException, OAuthException, IOException {
		expect(RedditManager.getInstance()).andThrow(new OAuthException("OH NO"));
		replay(RedditManager.class);
		new ShowerThoughtsSpeechletRequestStreamHandler();
	}
	
	@Test(expected=IOException.class)
	public void ioException() throws NetworkException, OAuthException, IOException {
		expect(RedditManager.getInstance()).andThrow(new IOException());
		replay(RedditManager.class);
		new ShowerThoughtsSpeechletRequestStreamHandler();
	}
}
