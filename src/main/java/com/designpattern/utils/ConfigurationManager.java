package com.designpattern.utils;

import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration utility to manage framework properties
 */
public class ConfigurationManager {
    private static final Logger log = LogUtils.getLogger(ConfigurationManager.class);
    private static final String CONFIG_FILE = "src/test/resources/config.properties";
    private static Properties properties;
    private static ConfigurationManager instance;
    
    private ConfigurationManager() {
        loadProperties();
    }
    
    public static ConfigurationManager getInstance() {
        if (instance == null) {
            synchronized (ConfigurationManager.class) {
                if (instance == null) {
                    instance = new ConfigurationManager();
                }
            }
        }
        return instance;
    }
    
    private void loadProperties() {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.load(fis);
            log.info("Configuration loaded successfully");
        } catch (IOException e) {
            log.error("Failed to load configuration file", e);
            throw new FrameworkException("Failed to load configuration", e);
        }
    }
    
    public String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new FrameworkException("Property not found: " + key);
        }
        return value;
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }
    
    public int getIntProperty(String key) {
        try {
            return Integer.parseInt(getProperty(key));
        } catch (NumberFormatException e) {
            throw new FrameworkException("Invalid integer property: " + key);
        }
    }
}