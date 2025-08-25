// change 5 - 09:22

package com.example.secretserver.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpDelete;
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
                request.addHeader("Cookie", "visid_incap_1873009=nzNfpneU/+vH; nlbi_1873009=ic7db48rc31M2aNeoAODJqAMAAC31VM36Y4yE1coN4s3G2Yd; incap_ses_68_1873009=h/5tIZ00TQ4xOGfzDpbxANjnq2gAAAAAaxK5fgXmKLZSKfg5Lky7Tg==");
                HttpResponse response = httpClient.execute(request);
                for (Header header : response.getAllHeaders()) {
                    logger.debug("Response header: {}: {}", header.getName(), header.getValue());
                }
                int statusCode = response.getStatusLine().getStatusCode();
                String json = EntityUtils.toString(response.getEntity());
                logger.debug("API response for secretId {}: {}", secretId, json);
                if (statusCode == 200) {
                    JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
                    if (jsonObject == null || !jsonObject.has("id") || !jsonObject.has("name") || !jsonObject.has("status")) {
                        logger.error("Invalid JSON response for secretId {}: {}", secretId, json);
                        return null;
                    }
                    String id = jsonObject.get("id").getAsString();
                    String name = jsonObject.get("name").getAsString();
                    String status = jsonObject.get("status").getAsString();
                    return new SecretDetails(id, name, status);
                } else {
                    logger.error("Get details failed for secretId {}: HTTP {}, Response: {}", secretId, statusCode, json);
                }
            } catch (IOException e) {
                logger.warn("Network issue for secretId {} on attempt {}/{}: {}", secretId, attempt, retries, e.getMessage());
            }
        }
        return null;
    }

    public boolean disableSecret(String secretId, int retries) {
        String url = baseUrl + "/secrets/" + secretId;
        for (int attempt = 1; attempt <= retries; attempt++) {
            try {
                HttpDelete request = new HttpDelete(url);
                request.addHeader("Authorization", "Bearer " + authToken);
                request.addHeader("Accept", "application/json");
                request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                request.addHeader("Cookie", "visid_incap_1873009=nzNfpneU/+vH; nlbi_1873009=ic7db48rc31M2aNeoAODJqAMAAC31VM36Y4yE1coN4s3G2Yd; incap_ses_68_1873009=h/5tIZ00TQ4xOGfzDpbxANjnq2gAAAAAaxK5fgXmKLZSKfg5Lky7Tg==");
                HttpResponse response = httpClient.execute(request);
                for (Header header : response.getAllHeaders()) {
                    logger.debug("Response header: {}: {}", header.getName(), header.getValue());
                }
                int statusCode = response.getStatusLine().getStatusCode();
                String json = EntityUtils.toString(response.getEntity());
                logger.debug("API response for disableSecret secretId {}: {}", secretId, json);
                if (statusCode == 200 || statusCode == 204) {
                    logger.info("Successfully disabled secretId {}", secretId);
                    return true;
                } else {
                    logger.error("Disable failed for secretId {}: HTTP {}, Response: {}", secretId, statusCode, json);
                }
            } catch (IOException e) {
                logger.warn("Network issue for secretId {} on attempt {}/{}: {}", secretId, attempt, retries, e.getMessage());
            }
        }
        return false;
    }
}
