package com.dotm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author dotm
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan({"com.dotm.**.mapper", "com.framework.**.mapper"})
public class AuthClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthClientApplication.class, args);
    }
}
