package com.vranec.jira;

import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.RestClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

@org.springframework.context.annotation.Configuration
public class JiraClientFactory {
    @Autowired
    private Configuration configuration;

    @Bean
    CustomJiraClient getCustomJiraClient() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        BasicCredentials creds = new BasicCredentials(configuration.getJiraUsername(), configuration.getJiraPassword());
        CustomJiraClient jira = new CustomJiraClient("https://monetamoneybank.atlassian.net/", creds);
        if (configuration.isIgnoreInvalidServerCertificate()) {
            RestClient restClient = jira.getRestClient();
            DefaultHttpClient httpClient = (DefaultHttpClient) restClient.getHttpClient();
            org.apache.http.conn.ssl.SSLSocketFactory sslsf = new org.apache.http.conn.ssl.SSLSocketFactory(
                    (chain, authType) -> true);
            httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, sslsf));
        }
        return jira;
    }
}
