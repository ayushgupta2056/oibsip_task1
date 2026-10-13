package com.paycheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class PayCheckApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayCheckApplication.class, args);
    }

    /** A shared clock, so tests can control "now" for time-based rules. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
