package com.vranec.configuration;

import com.vranec.csv.exporter.CsvExporter;
import com.vranec.jira.gateway.CustomJiraClient;
import com.vranec.timesheet.generator.Configuration;
import com.vranec.timesheet.generator.TimesheetGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;

/**
 * Entry point of the application. Starts {@link TimesheetGenerator} automatically (using @{@link javax.annotation.PostConstruct}.
 */
@SpringBootApplication
public class TimesheetGeneratorApplication {
    @Autowired
    private TimesheetGenerator timesheetGenerator;

    public static void main(String[] args) {
        SpringApplication.run(TimesheetGeneratorApplication.class, args);
    }

    @PostConstruct
    public void main() throws Exception {
        timesheetGenerator.generateTimesheet();
    }
}
