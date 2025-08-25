package com.example.secretserver.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    public static Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.error("Unable to find application.properties");
                throw new IOException("application.properties not found");
            }
            props.load(input);
        }
        return props;
    }
}