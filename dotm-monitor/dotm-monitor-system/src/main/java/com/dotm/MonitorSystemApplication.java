package com.dotm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author dotm
 */
@SpringBootApplication
@EnableScheduling
public class MonitorSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(MonitorSystemApplication.class, args);
    }
}
