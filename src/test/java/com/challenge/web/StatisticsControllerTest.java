package com.challenge.web;

import com.challenge.StatisticsApplication;
import com.challenge.transfer.StatisticsOutput;
import com.challenge.transfer.TransactionInput;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = StatisticsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class StatisticsControllerTest {

    @LocalServerPort
    private int port;

    @Value("${transaction.acceptance.time:5}")
    private int transactionTtl;

    @Autowired
    private TestRestTemplate restTemplate;
    private HttpHeaders headers;

    @Before
    public void setup() {
        headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
    }

    private String getUrlForUri(String uri) {
        return "http://localhost:" + port + uri;
    }

    @Test
    public void getEmptyStatistics() {
        StatisticsOutput defaultStatistics = new StatisticsOutput();

        ResponseEntity<StatisticsOutput> response = doRequest();
        assertThat("Status code should be 200", response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(defaultStatistics));
    }

    @Test
    public void getStatisticsForTwentyTransactions() {
        List<TransactionInput> transactions = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            TransactionInput transaction = new TransactionInput(
                    ThreadLocalRandom.current().nextDouble(0, 10000),
                    Instant.now().minus(1, ChronoUnit.SECONDS).toEpochMilli());
            transactions.add(transaction);
        }
        transactions.forEach(this::postTransaction);

        ResponseEntity<StatisticsOutput> response = doRequest();
        assertThat("Status code should be 200", response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(calculateStatistics(transactions)));

        try {
            Thread.sleep(transactionTtl * 1000);
        } catch (InterruptedException e) {
            fail("Thread should not be interrupted");
        }

        StatisticsOutput defaultStatistics = new StatisticsOutput();

        response = doRequest();
        assertThat("Status code should be 200", response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is(defaultStatistics));
    }

    private StatisticsOutput calculateStatistics(List<TransactionInput> transactions) {
        if (transactions.isEmpty()) {
            return new StatisticsOutput();
        }
        double min = 0;
        double max = 0;
        double sum = 0;
        long count = transactions.size();

        for (TransactionInput transaction : transactions) {
            double amount = transaction.getAmount();
            sum += amount;
            if (amount <= min || min == 0)
                min = amount;
            if (amount >= max) {
                max = amount;
            }
        }
        double average = sum / count;
        return new StatisticsOutput(sum, average, max, min, count);
    }

    private void postTransaction(TransactionInput transactionInput) {
        ResponseEntity<Void> response = doRequest(transactionInput);
        assertThat("Status code should be 201", response.getStatusCode(), is(HttpStatus.CREATED));
    }

    private ResponseEntity<Void> doRequest(TransactionInput transaction) {
        HttpEntity<TransactionInput> httpEntity = new HttpEntity<>(transaction, headers);
        return restTemplate.exchange(
                getUrlForUri("/transactions"),
                HttpMethod.POST,
                httpEntity,
                Void.class);
    }

    private ResponseEntity<StatisticsOutput> doRequest() {
        HttpEntity<TransactionInput> httpEntity = new HttpEntity<>(headers);
        return restTemplate.exchange(
                getUrlForUri("/statistics"),
                HttpMethod.GET,
                httpEntity,
                StatisticsOutput.class);
    }
}
