package com.vranec.jira;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import sun.misc.IOUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Getter
@Component
@PropertySource(value = "file:timesheet-generator.properties", ignoreResourceNotFound = true)
public class Configuration {
    @Value("${jira.url}")
    private String jiraUrl;
    @Value("${jira.username}")
    private String jiraUsername;
    @Value("${jira.password}")
    private String jiraPassword;
    @Value("${month.detection.subtract.days}")
    private int monthDetectionSubtractDays;
    @Value("${ignore.invalid.server.certificate}")
    private boolean ignoreInvalidServerCertificate;

    @PostConstruct
    public void createConfigurationFileIfNotExists() throws IOException {
        File configurationFile = new File("timesheet-generator.properties");
        if (!configurationFile.exists()) {
            Files.copy(getClass().getResourceAsStream("/default.properties"), configurationFile.toPath());
            log.error("Please edit the generated timesheet-generator.properties");
        }
    }
}
