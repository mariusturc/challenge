package com.challenge.service;

import com.challenge.domain.Statistic;
import com.challenge.transfer.StatisticsOutput;
import com.challenge.transfer.TransactionInput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StatisticsService {

    @Value("${transaction.acceptance.time:60}")
    private int transactionTtl;

    private final Map<Long, Statistic> statisticsBucketMap = new ConcurrentHashMap<>(transactionTtl);

    /**
     * Adds a {@code TransactionInput} to the {@link StatisticsService#statisticsBucketMap} if the
     * transactionTime is in the past, but not older then a predefined interval.
     *
     * @param input the TransactionInput
     * @return 201 if the transaction was successfully added; 204 if transaction is older than the predefined interval
     */
    public ResponseEntity addTransaction(TransactionInput input) {
        Instant now = Instant.now();
        Instant transactionTime = Instant.ofEpochMilli(input.getTimestamp());
        //assume that a timestamp in the future will also return 204 status
        if (!isValidTimestamp(now, transactionTime)) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }

        long transactionEpochSecond = transactionTime.getEpochSecond();

        statisticsBucketMap.merge(transactionEpochSecond, new Statistic(input.getAmount()), Statistic::merge);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    /**
     * Gets the transaction statistics for the past predefined interval.
     *
     * @return the statistics, not null
     */
    public StatisticsOutput getStatistics() {
        Instant now = Instant.now();
        Long thresholdTime = getStatisticsThresholdStartTime(now).getEpochSecond();
        StatisticsOutput result = new StatisticsOutput();

        statisticsBucketMap.entrySet()
                .forEach(e -> {
                    if (thresholdTime <= e.getKey()) {
                        Statistic value = e.getValue();
                        result.setSum(result.getSum() + value.getSum());
                        result.setCount(result.getCount() + value.getCount());
                        if (result.getMax() < value.getMax()) {
                            result.setMax(value.getMax());
                        }
                        if (result.getMin() > value.getMax() || result.getMin() == 0) {
                            result.setMin(value.getMin());
                        }
                    }
                });
        if (result.getCount() > 0) {
            result.setAvg(result.getSum() / result.getCount());
        }

        return result;
    }

    /**
     * Scheduled method that runs a bucket cleanup.
     */
    @Scheduled(fixedRate = 1000)
    public void cleanStatisticsMap() {
        Instant now = Instant.now();
        Long thresholdTime = getStatisticsThresholdStartTime(now).getEpochSecond();

        statisticsBucketMap.keySet().stream()
                .filter(statisticTimeStamp -> thresholdTime > statisticTimeStamp)
                .forEach(statisticsBucketMap::remove);
    }

    /**
     * Checks if an {@code Instant} is predefined threshold period of time, before another.
     *
     * @param now             the instant for which to check against
     * @param transactionTime the instant for which to check
     * @return true if the Instant is in the time interval (including the interval ends), false otherwise
     */
    private boolean isValidTimestamp(Instant now, Instant transactionTime) {
        long until = transactionTime.until(now, ChronoUnit.SECONDS);
        return until <= transactionTtl && until >= 0;
    }

    /**
     * Subtracts the threshold time(time duration in seconds which to calculate the statistics) from an {@code Instant}.
     *
     * @param now Instant from which to subtract
     * @return Instant minus the threshold time
     */
    private Instant getStatisticsThresholdStartTime(Instant now) {
        return now.minus(transactionTtl, ChronoUnit.SECONDS);
    }
}
