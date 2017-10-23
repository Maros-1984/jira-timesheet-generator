package com.vranec.timesheet.generator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.stream.Collectors.joining;

@RequiredArgsConstructor
public class TasksForDay {
    @Getter
    private final LocalDate date;
    private final Set<String> taskNames = new LinkedHashSet<>();

    void addTask(String taskName) {
        taskNames.add(taskName);
    }

    @Override
    public String toString() {
        return taskNames.stream().collect(joining(","));
    }
}
