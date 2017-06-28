package com.vranec.jira;

import net.rcarz.jiraclient.*;
import net.sf.json.JSON;
import net.sf.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

class CustomJiraClient extends JiraClient {
    CustomJiraClient(String uri, ICredentials creds) {
        super(uri, creds);
    }

    Issue.SearchResult searchIssues(String jql, String includedFields, Integer maxResults, String expand) {
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
            if (includedFields != null) {
                queryParams.put("fields", includedFields);
            }
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
}
