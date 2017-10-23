package com.vranec.configuration;

import com.vranec.csv.exporter.CsvExporter;
import com.vranec.jira.gateway.CustomJiraClient;
import com.vranec.timesheet.generator.TimesheetGenerator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = {TimesheetGenerator.class, ConfigurationFromPropertiesFile.class, CustomJiraClient.class, CsvExporter.class})
public class SpringContextConfiguration {
}
