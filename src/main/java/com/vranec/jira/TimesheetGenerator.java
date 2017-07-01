package com.vranec.jira;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.rcarz.jiraclient.ChangeLogEntry;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.Issue.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Generates timesheets from JIRA changelogs of tickets updated or watched by
 * current user.
 *
 * @author Maros Vranec
 */
@Slf4j
@Component
@Order(0)
public class TimesheetGenerator {
    @Autowired
    private Configuration configuration;
    @Autowired
    private CsvExporter csvExporter;
    @Autowired
    private CustomJiraClient jira;

    @PostConstruct
    public void main() throws Exception {
        if (configuration.getJiraUsername().isEmpty()) {
            return;
        }

        val startDate = LocalDate.now().minusDays(configuration.getMonthDetectionSubtractDays()).withDayOfMonth(1);
        val timesheet = parseTimesheet(startDate, jira);
        csvExporter.saveToCsv(startDate, timesheet);
        log.info("TIMESHEET GENERATED SUCCESSFULLY");
    }

    /**
     * Parses JIRA timesheets using JIRA REST API.
     *
     * @param startDate Starting date to start parsing.
     * @param jira      JIRA REST API client.
     * @return Timesheets based on JIRAs' changelogs.
     */
    private Map<LocalDate, IssuesStats> parseTimesheet(LocalDate startDate, CustomJiraClient jira) {
        ConcurrentMap<LocalDate, IssuesStats> timesheet = new ConcurrentHashMap<>();
        String jql = "updated >= '" + DateTimeFormatter.ofPattern("yyyy-M-d").format(startDate) + "' and (watcher = "
                + "currentUser()" + " or status changed by " + "currentUser()" + ")";
        log.info("Searching for issues by JQL: " + jql + "...");
        SearchResult result = jira.searchIssues(jql, "summary,comment", 1000, "changelog");

        String username = configuration.getJiraUsername();
        for (Issue issue : result.issues) {
            log.info("Parsing " + issue);
            for (ChangeLogEntry entry : issue.getChangeLog().getEntries()) {
                if (entry.getAuthor().getName().equals(username.split("@")[0])) {
                    LocalDate date = LocalDate.from(entry.getCreated().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                    timesheet.putIfAbsent(date, new IssuesStats());
                    timesheet.get(date).addIssue(issue);
                }
            }
        }
        return timesheet;
    }
}
