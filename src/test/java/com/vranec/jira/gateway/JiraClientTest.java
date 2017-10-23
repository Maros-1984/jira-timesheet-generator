package com.vranec.jira.gateway;

import com.vranec.configuration.ConfigurationFromPropertiesFile;
import com.vranec.configuration.SpringContextConfiguration;
import com.vranec.configuration.TimesheetGeneratorApplication;
import lombok.Data;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.RestClient;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.List;

import static java.time.LocalDate.now;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringContextConfiguration.class)
public class JiraClientTest {
    @SpyBean
    private CustomJiraClient jira;
    @MockBean
    private TimesheetGeneratorApplication application;
    @Mock
    private RestClient restClient;

    @Before
    public void setUp() throws Exception {
        Mockito.doReturn(restClient).when(jira).getRestClient();
        Mockito.doReturn(JSONObject.fromObject(new JiraIssuesHolder())).when(restClient).get((URI) null);
    }

    @Test
    public void getTasks_givenCurrentDate_shouldConvertJiraIssues() throws Exception {
        Assert.assertFalse(jira.getTasks(now()).iterator().hasNext());
    }

    @Data
    public static class JiraIssuesHolder {
        private List<Issue> issues;
    }
}
