package com.vranec.timesheet.generator;

import com.vranec.configuration.SpringContextConfiguration;
import com.vranec.configuration.TimesheetGeneratorApplication;
import com.vranec.jira.gateway.CustomJiraClient;
import net.rcarz.jiraclient.JiraException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Random;

import static java.util.stream.Collectors.toSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringContextConfiguration.class)
public class TimesheetGeneratorTest {
    @Autowired
    private TimesheetGenerator timesheetGenerator;
    @Autowired
    private Configuration configuration;
    @SpyBean
    private CustomJiraClient jira;
    @MockBean
    private TimesheetGeneratorApplication application;

    @Test
    public void generateTimesheet_givenOneTask_shouldGenerateReport() throws Exception {
        doReturn(oneIssueFromYesterday("SIMPLE_TASK")).when(jira).getTasks(any(LocalDate.class));
        timesheetGenerator.generateTimesheet();
        String csv = readExport();
        Assert.assertTrue(csv.contains("SIMPLE_TASK"));
        Assert.assertFalse(csv.contains("OTHER_TASK"));
    }

    private String readExport() throws IOException {
        return new String(Files.readAllBytes(Paths.get("export.csv")));
    }

    private Iterable<Task> oneIssueFromYesterday(String... issues) throws JiraException {
        return Arrays.stream(issues)
                .map(issueName -> Task.builder()
                        .date(LocalDate.now().minusDays(configuration.getMonthDetectionSubtractDays()))
                        .author(configuration.getJiraUsername().split("@")[0])
                        .name(issueName)
                        .build())
                .collect(toSet());
    }
}
