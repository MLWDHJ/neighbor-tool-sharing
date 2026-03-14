package com.neighbor.tool;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.neighbor.tool.repository")
@EnableScheduling
public class ToolSharingApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ToolSharingApplication.class, args);
    }
}
