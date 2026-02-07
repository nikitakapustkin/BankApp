package org.nikitakapustkin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BankServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankServiceApplication.class, args);
    }
}
