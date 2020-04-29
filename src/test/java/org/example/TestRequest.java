package org.example;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

public class TestRequest {

    private static final String ENDPOINT = "http://localhost:8082/";
    private String valueToBeSend = "1001";
    private JettyServer server;
    private HttpClient client;
    private List<String> responses;

    @Before
    public void setUp() throws Exception {
        client = HttpClient.newHttpClient();
        responses = Collections.synchronizedList(new ArrayList<>());
        server = new JettyServer();
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
        client = null;
        responses.clear();
    }

    @Test
    public void testPost() throws Exception {
        HttpResponse<String> response = makePostRequest(valueToBeSend);
        assertEquals(200, response.statusCode());
        assertNotNull(response);
    }

    @Test
    public void testQuery() throws IOException, InterruptedException {
        String[] numbers = {
                "1", "2", "3"
        };
        CountDownLatch countDownLatch = new CountDownLatch(numbers.length);
        String sum = sumNumbers(numbers);
        CompletableFuture<?> numbersFuture = sendNumbers(numbers, countDownLatch);
        countDownLatch.await();
        sendFinish();
        numbersFuture.join();

        Iterator<String> responsesIterator = responses.iterator();
        String first = responsesIterator.next();
        responsesIterator.forEachRemaining(next -> assertEquals(first, next));

        assertEquals(sum, first);
    }

    @Test
    public void testWrongDataInput() {
        String[] numbers = {"fish"};
        try {
            CountDownLatch countDownLatch = new CountDownLatch(numbers.length);
            sendNumbers(numbers, countDownLatch);
            //fail();
        } catch (NumberFormatException ex) {
            assertEquals("For input string: \"fish\"", ex.getMessage());
        }
    }


    @Test
    public void testPostFuture() throws IOException, InterruptedException {
        List<URI> uris = new ArrayList<URI>();
        uris.add(URI.create(ENDPOINT));

        HttpClient client = HttpClient.newHttpClient();
        List<HttpRequest> requests = uris.stream()
                .map(HttpRequest::newBuilder)
                .map(HttpRequest.Builder::build)
                .collect(toList());

        CompletableFuture<?> futures = CompletableFuture.allOf(requests.stream()
                .map(request -> client.sendAsync(request, ofString()))
                .toArray(CompletableFuture<?>[]::new));

        futures.join();
        assertNotNull(futures);
    }

    @Test
    public void testPostSimpleValue() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .POST(HttpRequest.BodyPublishers.ofString(valueToBeSend))
                .build();

        HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.discarding());
        responses.add(response.body().toString());
        for(String item : responses) {
            System.out.println("testPostSimpleValue: " + item);
        }

        assertEquals(200, response.statusCode());
        assertNotNull(response);
    }


    private HttpResponse<String> makePostRequest(String data) throws IOException, InterruptedException {
        return client.send(HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    //https://openjdk.java.net/groups/net/httpclient/recipes.html

    private CompletableFuture<?> sendNumbers(String[] numbers, CountDownLatch countDownLatch) {
        CompletableFuture<?>[] futures = Arrays.stream(numbers)
                .map(number -> CompletableFuture.runAsync(() -> {
                    HttpResponse<String> response = null;
                    countDownLatch.countDown();
                    try {
                        response = makePostRequest(number);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    assert response != null;
                    collectBodyResponse(response);
                }))
                .toArray(CompletableFuture<?>[]::new);

        return CompletableFuture.allOf(futures);
    }

    private void sendFinish() throws IOException, InterruptedException {
        HttpResponse<String> response = makePostRequest("finish");
        collectBodyResponse(response);
    }

    private void collectBodyResponse(HttpResponse<String> response) {
        responses.add(response.body().trim());
    }

    private String sumNumbers(String[] numbers) {
        BigInteger sum = new BigInteger("0");
        for (String number : numbers) {
            sum = sum.add(new BigInteger(number));
        }
        return sum.toString();
    }
}
