package com.vranec.timesheet.generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.time.LocalDate.now;

@Slf4j
@RequiredArgsConstructor
@Component
public class TimesheetGenerator {
    private final Configuration configuration;
    private final Exporter exporter;
    private final TaskSource taskSource;

    public void generateTimesheet() throws Exception {
        val startDate = now().minusDays(configuration.getMonthDetectionSubtractDays()).withDayOfMonth(1);
        val timesheet = parseTimesheetFrom(startDate);
        exporter.export(timesheet);
        log.info("TIMESHEET GENERATED SUCCESSFULLY");
    }

    private List<TasksForDay> parseTimesheetFrom(LocalDate startDate) {
        Iterable<Task> tasks = taskSource.getTasks(startDate);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        if (endDate.isAfter(now())) {
            endDate = now();
        }
        List<TasksForDay> tasksForDays = new ArrayList<>();
        ConcurrentMap<LocalDate, TasksForDay> timesheet = new ConcurrentHashMap<>();
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            TasksForDay tasksForDay = new TasksForDay(date);
            tasksForDays.add(tasksForDay);
            timesheet.putIfAbsent(date, tasksForDay);
        }

        tasks.forEach(task -> {
            log.info("Parsing " + task.getName());
            if (task.getAuthor().equals(configuration.getJiraUsername().split("@")[0])) {
                if (timesheet.containsKey(task.getDate())) {
                    timesheet.get(task.getDate()).addTask(task.getName());
                }
            }
        });
        return tasksForDays;
    }
}
