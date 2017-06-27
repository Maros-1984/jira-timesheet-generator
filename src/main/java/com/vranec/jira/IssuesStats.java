package com.vranec.jira;

import java.util.LinkedHashSet;
import java.util.Set;

import net.rcarz.jiraclient.Issue;

/**
 * Issues statistics for one date in calendar.
 * 
 * @author Maros Vranec
 */
public class IssuesStats {
    /** Found issues for this stats date. */
    private final Set<String> issues = new LinkedHashSet<String>();
    /** Logged work in seconds for this date. */
    private int seconds;

    /**
     * Add an issue to this date statistics.
     * 
     * @param issue
     *            Issue to be added.
     */
    public void addIssue(Issue issue) {
        if (issues.contains(issue.getKey())) {
            return;
        }
        issues.add(issue.getKey());
    }

    /**
     * @return Found issues for this stats date.
     */
    public String getIssues() {
        return issues.toString().replace("[", "").replace("]", "");
    }

    /**
     * @return Logged work in seconds for this date.
     */
    public int getLoggedSecondsOfWork() {
        return seconds;
    }
}
