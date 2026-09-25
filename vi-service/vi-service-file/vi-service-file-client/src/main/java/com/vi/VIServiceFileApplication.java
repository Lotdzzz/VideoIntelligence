package com.vi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * VI的文件上传模块
 *
 * @author dotm
 */
@SpringBootApplication
@MapperScan("com.vi.**.mapper")
@EnableFeignClients
public class VIServiceFileApplication {
    public static void main(String[] args) {
        SpringApplication.run(VIServiceFileApplication.class, args);
    }
}
