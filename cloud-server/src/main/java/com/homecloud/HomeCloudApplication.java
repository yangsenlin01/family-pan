package com.homecloud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.homecloud")
@MapperScan("com.homecloud.**.mapper")
public class HomeCloudApplication {
    public static void main(String[] args) {
        SpringApplication.run(HomeCloudApplication.class, args);
    }
}
