package com.vranec.timesheet.generator;

import java.util.List;

public interface Exporter {
    void export(List<TasksForDay> timesheet);
}
