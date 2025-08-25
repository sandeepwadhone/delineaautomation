// changes 3 00:22

package com.example.secretserver.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
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
    private final Gson gson;

    public SecretServerClient(String baseUrl, String authToken) {
        this.baseUrl = baseUrl;
        this.authToken = authToken;
        RequestConfig config = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build();
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();
        this.gson = new Gson();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public static class SecretDetails {
        public String id;
        public String name;
        public String status;

        public SecretDetails(String id, String name, String status) {
            this.id = id;
            this.name = name;
            this.status = status;
        }
    }

    public SecretDetails getSecretDetails(String secretId, int retries) {
        String url = baseUrl + "/secrets/" + secretId;
        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                HttpGet request = new HttpGet(url);
                request.addHeader("Authorization", "Bearer " + authToken);
                request.addHeader("Accept", "application/json");
                request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                HttpResponse response = httpClient.execute(request);
                for (Header header : response.getAllHeaders()) {
                    logger.debug("Response header: {}: {}", header.getName(), header.getValue());
                }
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String json = EntityUtils.toString(response.getEntity());
                    logger.debug("API response for secretId {}: {}", secretId, json);
                    JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
                    String id = jsonObject.get("id").getAsString();
                    String name = jsonObject.get("name").getAsString();
                    String status = jsonObject.get("status").getAsString();
                    return new SecretDetails(id, name, status);
                } else {
                    logger.error("Get details failed for secretId {}: HTTP {}", secretId, statusCode);
                }
            } catch (IOException e) {
                logger.warn("Network issue for secretId {} on attempt {}/{}: {}", secretId, attempt, retries, e.getMessage());
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
                request.addHeader("Accept", "application/json");
                request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                HttpResponse response = httpClient.execute(request);
                for (Header header : response.getAllHeaders()) {
                    logger.debug("Response header: {}: {}", header.getName(), header.getValue());
                }
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    return true;
                } else {
                    logger.error("Disable failed for secretId {}: HTTP {}", secretId, statusCode);
                }
            } catch (IOException e) {
                logger.warn("Network issue for secretId {} on attempt {}/{}: {}", secretId, attempt, retries, e.getMessage());
            }
        }
        return false;
    }
}
