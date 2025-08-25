package com.example.secretserver.api;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SecretServerClient {
    private static final Logger logger = LoggerFactory.getLogger(SecretServerClient.class);
    private final String baseUrl;
    private final String authToken;
    private final HttpClient httpClient;

    public SecretServerClient(String baseUrl, String authToken) {
        this.baseUrl = baseUrl;
        this.authToken = authToken;
        this.httpClient = HttpClients.createDefault();
    }

    // Inner class for secret details
    public static class SecretDetails {
        public String id;
        public String name;
        public String status;
        // Parse from JSON in real impl (use e.g., Gson or manual parsing)
    }

    public SecretDetails getSecretDetails(String secretId, int retries) {
        String url = baseUrl + "/secrets/" + secretId;
        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                HttpGet request = new HttpGet(url);
                request.addHeader("Authorization", "Bearer " + authToken);
                HttpResponse response = httpClient.execute(request);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String json = EntityUtils.toString(response.getEntity());
                    // TODO: Parse JSON to SecretDetails (add Gson dependency if needed)
                    SecretDetails details = new SecretDetails();  // Stub: populate from JSON
                    // e.g., details.name = jsonObject.get("name").getAsString();
                    return details;
                } else {
                    logger.error("Get details failed: {}", response.getStatusLine().getStatusCode());
                }
            } catch (IOException e) {
                logger.warn("Network issue on attempt {}/{}: {}", attempt, retries, e.getMessage());
            }
        }
        return null;
    }

    public boolean disableSecret(String secretId, int retries) {
        String url = baseUrl + "/secrets/" + secretId + "/disable";
        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                HttpPost request = new HttpPost(url);
                request.addHeader("Authorization", "Bearer " + authToken);
                HttpResponse response = httpClient.execute(request);
                if (response.getStatusLine().getStatusCode() == 200) {
                    return true;
                } else {
                    logger.error("Disable failed: {}", response.getStatusLine().getStatusCode());
                }
            } catch (IOException e) {
                logger.warn("Network issue on attempt {}/{}: {}", attempt, retries, e.getMessage());
            }
        }
        return false;
    }
}