package com.vranec.csv.exporter;

import com.vranec.timesheet.generator.Exporter;
import com.vranec.timesheet.generator.TasksForDay;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class CsvExporter implements Exporter {
    @Override
    public void export(List<TasksForDay> timesheet) {
        saveToCsv(timesheet);
    }

    private void saveToCsv(List<TasksForDay> timesheet) {
        FileWriter writer = null;
        try {
            writer = new FileWriter("export.csv");
            writeCsv(timesheet, writer);
            writer.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            closeWriter(writer);
        }
    }

    private void writeCsv(List<TasksForDay> timesheet, FileWriter writer) throws IOException {
        log.info("Saving to CSV...");
        CSVPrinter csv = createCsvPrinter(writer);
        printCsvHeader(csv);
        writeTasks(csv, timesheet);
        csv.close();
    }

    private void writeTasks(CSVPrinter csv, List<TasksForDay> timesheet) throws IOException {
        for (TasksForDay tasksForDay : timesheet) {
            writeTask(csv, tasksForDay);
        }
    }

    private void writeTask(CSVPrinter csv, TasksForDay tasksForDay) throws IOException {
        csv.print(DateTimeFormatter.ofPattern("d.M.yyyy").format(tasksForDay.getDate()));
        csv.print(tasksForDay);
        csv.print(8);
        csv.println();
    }

    private void printCsvHeader(CSVPrinter csv) throws IOException {
        csv.printRecord("Date", "Tasks");
    }

    private CSVPrinter createCsvPrinter(FileWriter writer) throws IOException {
        return new CSVPrinter(writer, CSVFormat.EXCEL.withDelimiter(';'));
    }

    private void closeWriter(FileWriter writer) {
        if (writer == null) {
            return;
        }
        try {
            writer.close();
        } catch (IOException ignored) {
        }
    }
}
