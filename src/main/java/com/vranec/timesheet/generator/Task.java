package com.vranec.timesheet.generator;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class Task {
    private LocalDate date;
    private String name;
    private String author;
}
