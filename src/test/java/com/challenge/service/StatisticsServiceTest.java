package com.challenge.service;

import com.challenge.transfer.StatisticsOutput;
import com.challenge.transfer.TransactionInput;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class StatisticsServiceTest {

    private StatisticsService statisticsService;

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        statisticsService = new StatisticsService();
        Class statisticsServiceClass = StatisticsService.class;
        Field field = statisticsServiceClass.getDeclaredField("transactionTtl");
        field.setAccessible(true);
        field.set(statisticsService, 60);
    }

    @Test
    public void testAddTransactionWithFutureDate() {
        TransactionInput transaction = getTransaction(Instant.now().plus(Duration.ofSeconds(10)), 5);
        ResponseEntity responseEntity = statisticsService.addTransaction(transaction);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.NO_CONTENT));
    }

    @Test
    public void testAddTransactionWithPastDate() {
        TransactionInput transaction = getTransaction(Instant.now().minus(Duration.ofSeconds(61)), 5);
        ResponseEntity responseEntity = statisticsService.addTransaction(transaction);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.NO_CONTENT));
    }

    @Test
    public void testAddTransactionSuccessfully() {
        TransactionInput transaction = getTransaction(Instant.now().minus(Duration.ofSeconds(10)), 5);
        ResponseEntity responseEntity = statisticsService.addTransaction(transaction);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.CREATED));

        StatisticsOutput statistics = statisticsService.getStatistics();
        assertThat(statistics, notNullValue());
        assertThat(statistics.getSum(), is(5d));
        assertThat(statistics.getAvg(), is(5d));
        assertThat(statistics.getMax(), is(5d));
        assertThat(statistics.getMin(), is(5d));
        assertThat(statistics.getCount(), is(1L));
    }

    @Test
    public void testGetStatisticsNoTransactions() {
        StatisticsOutput statistics = statisticsService.getStatistics();
        assertThat(statistics, notNullValue());
        assertThat(statistics.getSum(), is(0d));
        assertThat(statistics.getAvg(), is(0d));
        assertThat(statistics.getMax(), is(0d));
        assertThat(statistics.getMin(), is(0d));
        assertThat(statistics.getCount(), is(0L));
    }

    @Test
    public void getStatistics() {
        TransactionInput transaction = getTransaction(Instant.now().minus(Duration.ofSeconds(10)), 5);
        ResponseEntity responseEntity = statisticsService.addTransaction(transaction);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.CREATED));

        transaction.setAmount(3.00);
        responseEntity = statisticsService.addTransaction(transaction);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.CREATED));

        transaction.setAmount(4.00);
        responseEntity = statisticsService.addTransaction(transaction);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.CREATED));

        StatisticsOutput statistics = statisticsService.getStatistics();

        assertThat(statistics, notNullValue());
        assertThat(statistics.getSum(), is(12d));
        assertThat(statistics.getAvg(), is(4d));
        assertThat(statistics.getMax(), is(5d));
        assertThat(statistics.getMin(), is(3d));
        assertThat(statistics.getCount(), is(3L));
    }

    @Test
    public void getStatisticsWithTransactionsAtDifferentDates() {
        TransactionInput transaction = getTransaction(Instant.now().minus(Duration.ofSeconds(10)), 5);
        ResponseEntity responseEntity = statisticsService.addTransaction(transaction);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.CREATED));

        transaction = getTransaction(Instant.now().minus(Duration.ofSeconds(15)), 3);
        responseEntity = statisticsService.addTransaction(transaction);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.CREATED));

        transaction.setAmount(4);
        responseEntity = statisticsService.addTransaction(transaction);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.CREATED));

        StatisticsOutput statistics = statisticsService.getStatistics();

        assertThat(statistics, notNullValue());
        assertThat(statistics.getSum(), is(12d));
        assertThat(statistics.getAvg(), is(4d));
        assertThat(statistics.getMax(), is(5d));
        assertThat(statistics.getMin(), is(3d));
        assertThat(statistics.getCount(), is(3L));
    }

    private TransactionInput getTransaction(Instant time, int amount) {
        TransactionInput transactionInput = new TransactionInput();
        transactionInput.setAmount(amount);
        transactionInput.setTimestamp(time.toEpochMilli());
        return transactionInput;
    }
}
