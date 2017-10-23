package com.vranec.timesheet.generator;

import java.time.LocalDate;

public interface TaskSource {
    Iterable<Task> getTasks(LocalDate startDate);
}
