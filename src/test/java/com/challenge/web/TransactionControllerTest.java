package com.challenge.web;

import com.challenge.StatisticsApplication;
import com.challenge.transfer.TransactionInput;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = StatisticsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    private HttpHeaders headers;
    private String url;

    @Before
    public void setup() {
        headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        url = "http://localhost:" + port + "/transactions";
    }

    @Test
    public void postValidTransaction() {
        TransactionInput transaction = new TransactionInput(54.10, Instant.now().toEpochMilli());
        ResponseEntity<Void> response = doRequest(transaction);
        assertThat("Status code should be 201", response.getStatusCode(), is(HttpStatus.CREATED));
    }

    @Test
    public void postOldTransaction() {
        TransactionInput transaction = new TransactionInput(10.00, Instant.now().minus(61, ChronoUnit.SECONDS).toEpochMilli());
        ResponseEntity<Void> response = doRequest(transaction);
        assertThat("Status code should be 204",response.getStatusCode(), is(HttpStatus.NO_CONTENT));
    }

    @Test
    public void postFutureTransaction() {
        TransactionInput transaction = new TransactionInput(3.2, Instant.now().plus(2, ChronoUnit.SECONDS).toEpochMilli());
        ResponseEntity<Void> response = doRequest(transaction);
        assertThat("Status code should be 204",response.getStatusCode(), is(HttpStatus.NO_CONTENT));
    }

    @Test
    public void postInvalidAmountTransaction() {
        TransactionInput transaction = new TransactionInput(-3.2, Instant.now().minus(1, ChronoUnit.SECONDS).toEpochMilli());
        ResponseEntity<Exception> response = doRequestErrorResp(transaction);
        assertThat("Status code should be 400",response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void postInvalidTimestampTransaction() {
        TransactionInput transaction = new TransactionInput(3.2, -1);
        ResponseEntity<Exception> response = doRequestErrorResp(transaction);
        assertThat("Status code should be 400",response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    private ResponseEntity<Void> doRequest(TransactionInput transaction) {
        HttpEntity<TransactionInput> httpEntity = getTransactionInputHttpEntity(transaction);
        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                Void.class);
    }

    private ResponseEntity<Exception> doRequestErrorResp(TransactionInput transaction) {
        HttpEntity<TransactionInput> httpEntity = getTransactionInputHttpEntity(transaction);
        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                Exception.class);
    }

    private HttpEntity<TransactionInput> getTransactionInputHttpEntity(TransactionInput transaction) {
        return new HttpEntity<>(transaction, headers);
    }
}
