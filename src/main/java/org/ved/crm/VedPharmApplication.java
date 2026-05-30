package org.ved.crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VedPharmApplication {

    public static void main(String[] args) {
        SpringApplication.run(VedPharmApplication.class, args);
    }

}
