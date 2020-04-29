package org.example;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class MockJetty {

    private static final String ENDPOINT = "http://localhost:8082/";
    private String valueToBeSend = "1";
    @Mock
    private JettyServer server;

    @Mock
    private HttpClient httpClient;

    private HttpClient client;

    @Before
    public void setup() throws Exception {
        server = mock(JettyServer.class);
        httpClient = mock(HttpClient.class);
        client = HttpClient.newHttpClient();
    }

    @After
    public void stopJetty() throws Exception {
        server.stop();
    }

    @Test
    public void shouldStartJettyServer() throws Exception {
        server.start();
        verify(server).start();
    }

    @Test
    public void testNegative() throws InterruptedException, IOException {
        try {
            server.start();
            HttpClient httpClient = Mockito.mock(HttpClient.class);
            Mockito.when(
                    httpClient.send(HttpRequest.newBuilder()
                            .uri(URI.create(ENDPOINT))
                            .POST(HttpRequest.BodyPublishers.ofString(valueToBeSend))
                            .build(), ofString())
            ).thenThrow(InterruptedException.class);

            HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .POST(HttpRequest.BodyPublishers.ofString(valueToBeSend))
                    .build(), HttpResponse.BodyHandlers.ofString());

            assertNull(response);
        } catch (InterruptedException ex) {
            assertNull(ex.getMessage());
            //assertThat(null, ex.getMessage(), is(null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}