package com.challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StatisticsApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(StatisticsApplication.class, args);
    }
}
