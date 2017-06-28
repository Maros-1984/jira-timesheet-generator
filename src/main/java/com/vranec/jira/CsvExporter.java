package com.vranec.jira;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
class CsvExporter {
    /**
     * Saves timesheets to CSV.
     *
     * @param startDate Start date of the timesheets.
     * @param timesheet Timesheets themselves.
     * @throws IOException In case writing CSV goes wrong.
     */
    void saveToCsv(LocalDate startDate, Map<LocalDate, IssuesStats> timesheet) throws IOException {
        log.info("Saving to CSV...");

        FileWriter writer = null;
        try {
            writer = new FileWriter("vykaz.csv");
            CSVPrinter csv = new CSVPrinter(writer, CSVFormat.EXCEL.withDelimiter(';'));
            csv.printRecord("Datum", "Cinnost", "Hodin");
            LocalDate inMonth = startDate.plusMonths(1);
            if (inMonth.isAfter(LocalDate.now())) {
                inMonth = LocalDate.now();
            }
            DateTimeFormatter simpleDateFormat = DateTimeFormatter.ofPattern("d.M.yyyy");

            for (LocalDate date = startDate; date.isBefore(inMonth); date = date.plusDays(1)) {
                csv.print(simpleDateFormat.format(date));
                IssuesStats issues = timesheet.get(date);
                if (issues != null) {
                    csv.print(issues.getIssues());
                    csv.print(8);
                } else {
                    csv.print(null);
                    csv.print(null);
                    csv.print(null);
                    csv.print(null);
                }
                csv.println();
            }

            csv.close();
            writer.close();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e1) {
                }
            }
        }
    }
}
