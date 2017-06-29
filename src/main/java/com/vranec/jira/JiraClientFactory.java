package com.vranec.jira;

import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.RestClient;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.params.ConnRoutePNames;
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
    CustomJiraClient getCustomJiraClient() throws UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException {
        BasicCredentials creds = new BasicCredentials(configuration.getJiraUsername(), configuration.getJiraPassword());
        CustomJiraClient jira = new CustomJiraClient(configuration.getJiraUrl(), creds);
        if (configuration.isIgnoreInvalidServerCertificate()) {
            DefaultHttpClient httpClient = getDefaultHttpClient(jira);
            org.apache.http.conn.ssl.SSLSocketFactory sslsf = new org.apache.http.conn.ssl.SSLSocketFactory(
                    (chain, authType) -> true);
            httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, sslsf));
        }
        if (!configuration.getProxyUrl().isEmpty()) {
            DefaultHttpClient httpclient = getDefaultHttpClient(jira);
            httpclient.getCredentialsProvider().setCredentials(new AuthScope(configuration.getProxyUrl(),
                    configuration.getProxyPort()), new UsernamePasswordCredentials(configuration.getProxyUsername(),
                    configuration.getProxyPassword()));
            HttpHost proxy = new HttpHost(configuration.getProxyUrl(), configuration.getProxyPort());
            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
        return jira;
    }

    private DefaultHttpClient getDefaultHttpClient(CustomJiraClient jira) {
        RestClient restClient = jira.getRestClient();
        return (DefaultHttpClient) restClient.getHttpClient();
    }
}
