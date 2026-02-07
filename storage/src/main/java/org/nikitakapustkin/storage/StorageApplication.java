package org.nikitakapustkin.storage;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class StorageApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(StorageApplication.class, args);
    }
}
