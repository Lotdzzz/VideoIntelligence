package com.dotm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author dotm
 */
@SpringBootApplication
@MapperScan("com.dotm.**.mapper")
@EnableDiscoveryClient
public class MonitorAuthClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(MonitorAuthClientApplication.class, args);
    }
}
