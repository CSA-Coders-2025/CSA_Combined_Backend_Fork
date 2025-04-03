package com.nighthawk.spring_portfolio;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.File;
// @SpringBootApplication annotation is the key to building web applications with Java https://spring.io/projects/spring-boot
@SpringBootApplication
public class Main {

    // Starts a spring application as a stand-alone application from the main method
    public static void main(String[] args) {
        File file = new File("/home/mwang/nighthawk/calendar_backend/volumes/sqlite.db");
        file.delete();
        SpringApplication.run(Main.class, args);
    }
    
}   