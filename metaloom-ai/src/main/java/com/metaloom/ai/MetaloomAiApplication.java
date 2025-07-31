package com.metaloom.ai;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@ComponentScan(basePackages = {"com.metaloom"})
public class MetaloomAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetaloomAiApplication.class, args);
    }

}