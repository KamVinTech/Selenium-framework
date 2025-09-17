package com.designpattern.config;

import com.designpattern.utils.LogUtils;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Manages dynamic configuration adjustments based on runtime metrics
 */
@SuppressWarnings("unused")
public class DynamicConfig {
    private static final Logger log = LogUtils.getLogger(DynamicConfig.class);
    private final Map<String, Object> runtimeConfig = new ConcurrentHashMap<>();
    private final Map<String, ConfigurationRule> configRules = new ConcurrentHashMap<>();
    
    public DynamicConfig() {
        initializeDefaultConfiguration();
        initializeDefaultRules();
    }
    
    private void initializeDefaultConfiguration() {
        // Default timeout values
        runtimeConfig.put("implicit.wait", Duration.ofSeconds(10));
        runtimeConfig.put("explicit.wait", Duration.ofSeconds(20));
        runtimeConfig.put("page.load.timeout", Duration.ofSeconds(30));
        runtimeConfig.put("script.timeout", Duration.ofSeconds(10));
        
        // Default polling intervals
        runtimeConfig.put("polling.interval", Duration.ofMillis(500));
        runtimeConfig.put("polling.max.interval", Duration.ofSeconds(2));
        
        // Default retry settings
        runtimeConfig.put("retry.max.attempts", 3);
        runtimeConfig.put("retry.backoff.multiplier", 1.5);
        
        // Performance thresholds
        runtimeConfig.put("network.latency.threshold", 1000L); // ms
        runtimeConfig.put("memory.usage.threshold", 80L); // percent
    }
    
    private void initializeDefaultRules() {
        // Rule for adjusting timeouts based on network latency
        addConfigurationRule(
            "dynamic.timeout.rule",
            metrics -> {
                Long latency = (Long) metrics.get("network.latency");
                if (latency != null && latency > getThreshold("network.latency.threshold")) {
                    // Increase timeouts proportionally
                    adjustTimeouts(latency);
                    return true;
                }
                return false;
            }
        );
        
        // Rule for adjusting polling intervals based on CPU usage
        addConfigurationRule(
            "dynamic.polling.rule",
            metrics -> {
                Double cpuUsage = (Double) metrics.get("cpu.usage");
                if (cpuUsage != null && cpuUsage > 70) {
                    // Increase polling intervals when CPU is high
                    adjustPollingIntervals(cpuUsage);
                    return true;
                }
                return false;
            }
        );
    }
    
    public void adjustConfiguration(Map<String, Object> metrics) {
        boolean configChanged = false;
        
        for (ConfigurationRule rule : configRules.values()) {
            try {
                if (rule.evaluate(metrics)) {
                    configChanged = true;
                }
            } catch (Exception e) {
                log.error("Error evaluating configuration rule: {}", e.getMessage());
            }
        }
        
        if (configChanged) {
            log.info("Configuration updated based on metrics: {}", metrics);
        }
    }
    
    private void adjustTimeouts(Long latency) {
        Duration currentExplicitWait = (Duration) get("explicit.wait");
        Duration adjustedWait = Duration.ofMillis(
            Math.min(
                currentExplicitWait.toMillis() * 2,
                60000 // Max 1 minute
            )
        );
        set("explicit.wait", adjustedWait);
        
        // Similarly adjust other timeouts
        Duration pageLoadTimeout = (Duration) get("page.load.timeout");
        set("page.load.timeout", Duration.ofMillis(
            Math.min(pageLoadTimeout.toMillis() * 2, 120000) // Max 2 minutes
        ));
    }
    
    private void adjustPollingIntervals(Double cpuUsage) {
        Duration currentInterval = (Duration) get("polling.interval");
        Duration maxInterval = (Duration) get("polling.max.interval");
        
        Duration adjustedInterval = Duration.ofMillis(
            Math.min(
                currentInterval.toMillis() * 2,
                maxInterval.toMillis()
            )
        );
        
        set("polling.interval", adjustedInterval);
    }
    
    public void set(String key, Object value) {
        runtimeConfig.put(key, value);
        log.debug("Configuration updated: {} = {}", key, value);
    }
    
    public Object get(String key) {
        return runtimeConfig.get(key);
    }
    
    public Duration getDuration(String key) {
        Object value = get(key);
        if (value instanceof Duration) {
            return (Duration) value;
        }
        throw new IllegalStateException("Configuration value is not a Duration: " + key);
    }
    
    public Long getThreshold(String key) {
        Object value = get(key);
        if (value instanceof Long) {
            return (Long) value;
        }
        throw new IllegalStateException("Configuration value is not a Long: " + key);
    }
    
    public void addConfigurationRule(String ruleId, ConfigurationRule rule) {
        configRules.put(ruleId, rule);
    }
    
    public void removeConfigurationRule(String ruleId) {
        configRules.remove(ruleId);
    }
    
    @FunctionalInterface
    public interface ConfigurationRule {
        boolean evaluate(Map<String, Object> metrics);
    }
    
    public Map<String, Object> getCurrentConfiguration() {
        return new HashMap<>(runtimeConfig);
    }
    
    public int getDefaultTimeout() {
        return (int) runtimeConfig.getOrDefault("default.timeout", 30);
    }
}