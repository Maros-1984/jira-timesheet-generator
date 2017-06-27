package com.vranec.jira;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the application. Starts {@link TimesheetGenerator} automatically (using @{@link javax.annotation.PostConstruct}.
 */
@SpringBootApplication
public class TimesheetGeneratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(TimesheetGeneratorApplication.class, args);
    }
}
