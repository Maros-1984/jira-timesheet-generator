package com.vranec.jira.gateway;

import com.vranec.timesheet.generator.Task;
import com.vranec.timesheet.generator.TaskSource;
import lombok.extern.slf4j.Slf4j;
import net.rcarz.jiraclient.*;
import net.sf.json.JSON;
import net.sf.json.JSONObject;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CustomJiraClient extends JiraClient implements TaskSource {
    CustomJiraClient(String uri, ICredentials creds) {
        super(uri, creds);
    }

    private Issue.SearchResult searchIssues(String jql, Integer maxResults, String expand) {
        final String j = jql;
        JSON result;

        try {
            Map<String, String> queryParams = new HashMap<String, String>() {
                {
                    put("jql", j);
                }
            };
            if (maxResults != null) {
                queryParams.put("maxResults", String.valueOf(maxResults));
            }
            queryParams.put("fields", "summary,comment");
            if (expand != null) {
                queryParams.put("expand", expand);
            }

            URI searchUri = getRestClient().buildURI(Resource.getBaseUri() + "search", queryParams);
            result = getRestClient().get(searchUri);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to search issues", ex);
        }

        if (!(result instanceof JSONObject)) {
            throw new IllegalStateException("JSON payload is malformed");
        }

        Issue.SearchResult sr = new Issue.SearchResult();
        Map map = (Map) result;

        sr.start = Field.getInteger(map.get("startAt"));
        sr.max = Field.getInteger(map.get("maxResults"));
        sr.total = Field.getInteger(map.get("total"));
        sr.issues = Field.getResourceArray(Issue.class, map.get("issues"), getRestClient());

        return sr;
    }

    public Iterable<Task> getTasks(LocalDate startDate) {
        String jql = "updated >= '" + DateTimeFormatter.ofPattern("yyyy-M-d").format(startDate) + "' and (watcher = "
                + "currentUser()" + " or status changed by " + "currentUser()" + ")";
        log.info("Searching for issues by JQL: " + jql + "...");
        Issue.SearchResult result = searchIssues(jql, 1000, "changelog");
        return convert(result);
    }

    private Collection<Task> convert(Issue.SearchResult result) {
        Collection<Task> tasks = new ArrayList<>();
        for (Issue issue : result.issues) {
            for (ChangeLogEntry entry : issue.getChangeLog().getEntries()) {
                tasks.add(convert(issue, entry));
            }
        }
        return tasks;
    }

    private Task convert(Issue issue, ChangeLogEntry entry) {
        LocalDate date = LocalDate.from(entry.getCreated().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        return Task.builder()
                .date(date)
                .name(issue.getKey())
                .author(entry.getAuthor().getName())
                .build();
    }
}
