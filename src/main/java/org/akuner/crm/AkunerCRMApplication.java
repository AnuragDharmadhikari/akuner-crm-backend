package org.akuner.crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AkunerCRMApplication {

    public static void main(String[] args) {
        SpringApplication.run(AkunerCRMApplication.class, args);
    }

}
