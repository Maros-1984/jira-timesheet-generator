package com.vranec.jira;

import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import sun.misc.IOUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by Maros on 06/27/2017.
 */
@Component
@PropertySource(value = "timesheet-generator.properties", ignoreResourceNotFound = true)
public class Configuration {
    @PostConstruct
    public void createConfigurationFileIfNotExists() throws IOException {
        File configurationFile = new File("timesheet-generator.properties");
        if (!configurationFile.exists()) {
            Files.copy(getClass().getResourceAsStream("/default.properties"), configurationFile.toPath());
        }
    }
}
