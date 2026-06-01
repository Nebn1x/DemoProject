package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // вмикає @Scheduled (CleanupScheduler)
@EnableAsync        // вмикає @Async (RequestLogService.saveLogAsync)
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
