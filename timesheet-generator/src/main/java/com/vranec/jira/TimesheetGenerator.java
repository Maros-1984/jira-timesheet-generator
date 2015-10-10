package com.vranec.jira;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.time.DateUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;

import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.ChangeLogEntry;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.Issue.SearchResult;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;

/**
 * Generates timesheets from JIRA changelogs of tickets updated or watched by
 * current user.
 * 
 * @author Maros Vranec
 */
public class TimesheetGenerator {
    /**
     * Starting point of the application.
     * 
     * @param args
     * @throws Exception
     *             In case something went terribly wrong.
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println(
                    "Usage: java -jar timesheet-generator.jar yourJiraUserName yourJiraPassword [month number, e.g. 10 for October, default is current month]");
            return;
        }

        String username = args[0];
        String password = args[1];
        Date startDate = DateUtils.truncate(new Date(), Calendar.MONTH);
        if (args.length > 2) {
            int month = Integer.parseInt(args[2]) - 1;
            startDate = DateUtils.setMonths(startDate, month);
        }

        try {
            JiraClient jira = prepareJiraClient(username, password);
            Map<Date, Set<String>> timesheet = parseTimesheet(username, startDate, jira);
            System.out.println();
            System.out.println("Saving to CSV...");
            saveToCsv(startDate, timesheet);
            System.out.println();
            System.out.println("TIMESHEET GENERATED SUCCESSFULLY");
        } catch (JiraException ex) {
            System.err.println(ex.getMessage());

            if (ex.getCause() != null)
                System.err.println(ex.getCause().getMessage());
        } catch (IOException ex) {
            System.err.println(ex.getMessage());

            if (ex.getCause() != null)
                System.err.println(ex.getCause().getMessage());
        }
    }

    /**
     * Parses JIRA timesheets using JIRA REST API.
     * 
     * @param username
     *            Username for filtering.
     * @param startDate
     *            Starting date to start parsing.
     * @param jira
     *            JIRA REST API client.
     * @return Timesheets based on JIRAs' changelogs.
     * @throws JiraException
     *             In case anything went wrong.
     */
    private static Map<Date, Set<String>> parseTimesheet(String username, Date startDate, JiraClient jira)
            throws JiraException {
        Map<Date, Set<String>> timesheet = new HashMap<Date, Set<String>>();
        SearchResult result = jira.searchIssues("updated >= '" + new SimpleDateFormat("yyyy-M-d").format(startDate)
                + "' and (watcher = " + username + " or status changed by " + username + ")", "summary", "changelog");

        System.out.print("Parsing ");
        for (Issue issue : result.issues) {
            System.out.print(issue + " ");
            for (ChangeLogEntry entry : issue.getChangeLog().getEntries()) {
                if (entry.getAuthor().getName().equals(username)) {
                    Date date = DateUtils.truncate(entry.getCreated(), Calendar.DATE);
                    if (!timesheet.containsKey(date)) {
                        timesheet.put(date, new LinkedHashSet<String>());
                    }
                    timesheet.get(date).add(issue.getKey());
                }
            }
        }
        return timesheet;
    }

    /**
     * Prepares JIRA REST API client. BEWARE: Bypasses SSL certificate
     * verification, trusts even fake jira.abank.cz (boo hoo).
     * 
     * @param username
     *            Username used for login.
     * @param password
     *            Password used for login.
     * @return JIRA REST API client.
     * @throws Exception
     *             In case anything went wrong.
     */
    private static JiraClient prepareJiraClient(String username, String password) throws Exception {
        BasicCredentials creds = new BasicCredentials(username, password);
        JiraClient jira1 = new JiraClient("https://jira.abank.cz/", creds);
        Field field = jira1.getClass().getDeclaredField("restclient");
        field.setAccessible(true);
        Object restClient = field.get(jira1);
        Field declaredField = restClient.getClass().getDeclaredField("httpClient");
        declaredField.setAccessible(true);
        DefaultHttpClient httpClient = (DefaultHttpClient) declaredField.get(restClient);
        org.apache.http.conn.ssl.SSLSocketFactory sslsf = new org.apache.http.conn.ssl.SSLSocketFactory(
                new TrustStrategy() {
                    public boolean isTrusted(final X509Certificate[] chain, String authType)
                            throws CertificateException {
                        return true;
                    }

                });
        httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, sslsf));
        JiraClient jira = jira1;
        return jira;
    }

    /**
     * Saves timesheets to CSV.
     * 
     * @param startDate
     *            Start date of the timesheets.
     * @param timesheet
     *            Timesheets themselves.
     * @throws IOException
     *             In case writing CSV goes wrong.
     */
    private static void saveToCsv(Date startDate, Map<Date, Set<String>> timesheet) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter("vykaz.csv");
            CSVPrinter csv = new CSVPrinter(writer, CSVFormat.EXCEL.withDelimiter(';'));
            csv.printRecord("Datum", "Èinnost", "Hodin");
            Date inMonth = DateUtils.addMonths(startDate, 1);
            if (inMonth.after(new Date())) {
                inMonth = new Date();
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d.M.yyyy");

            for (Date date = startDate; date.before(inMonth); date = DateUtils.addDays(date, 1)) {
                csv.print(simpleDateFormat.format(date));
                Set<String> issues = timesheet.get(date);
                if (issues != null) {
                    csv.print(issues.toString().replace("[", "").replace("]", ""));
                    csv.print(8);
                } else {
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