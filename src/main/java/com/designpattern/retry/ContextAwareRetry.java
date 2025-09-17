package com.designpattern.retry;

import com.designpattern.utils.LogUtils;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * Provides context-aware retry capabilities based on runtime conditions
 */
public class ContextAwareRetry {
    
    @FunctionalInterface
    public interface BooleanOperation {
        boolean execute() throws Exception;
    }
    private static final Logger log = LogUtils.getLogger(ContextAwareRetry.class);
    private final WebDriver driver;
    private final Map<Context, RetryStrategy> contextStrategies = new HashMap<>();
    
    public ContextAwareRetry(WebDriver driver) {
        this.driver = driver;
        initializeDefaultStrategies();
    }
    
    private void initializeDefaultStrategies() {
        // Default strategy for good network conditions
        contextStrategies.put(
            new Context(NetworkCondition.GOOD, BrowserState.STABLE),
            new RetryStrategy(3, 500) // 3 retries, 500ms delay
        );
        
        // Strategy for poor network conditions
        contextStrategies.put(
            new Context(NetworkCondition.POOR, BrowserState.STABLE),
            new RetryStrategy(5, 1000) // 5 retries, 1000ms delay
        );
        
        // Strategy for unstable browser state
        contextStrategies.put(
            new Context(NetworkCondition.GOOD, BrowserState.UNSTABLE),
            new RetryStrategy(4, 750) // 4 retries, 750ms delay
        );
        
        // Strategy for worst conditions
        contextStrategies.put(
            new Context(NetworkCondition.POOR, BrowserState.UNSTABLE),
            new RetryStrategy(7, 1500) // 7 retries, 1500ms delay
        );
    }
    
    public <T> T executeWithRetry(Callable<T> action) throws Exception {
        return doExecuteWithRetry(action);
    }
    
    public boolean executeWithRetry(BooleanOperation operation) throws Exception {
        return doExecuteWithRetry(() -> {
            operation.execute();
            return true;
        });
    }
    
    private <T> T doExecuteWithRetry(Callable<T> action) throws Exception {
        Context currentContext = analyzeExecutionContext();
        RetryStrategy strategy = contextStrategies.getOrDefault(
            currentContext,
            new RetryStrategy(3, 500) // Default fallback strategy
        );
        
        Exception lastException = null;
        int attempts = 0;
        
        while (attempts < strategy.getMaxRetries()) {
            try {
                return action.call();
            } catch (Exception e) {
                lastException = e;
                log.debug("Retry attempt {} failed: {}", attempts + 1, e.getMessage());
                
                if (shouldContinueRetrying(e)) {
                    Thread.sleep(strategy.getDelayMs());
                    attempts++;
                    
                    // Re-analyze context after failure
                    Context newContext = analyzeExecutionContext();
                    if (!newContext.equals(currentContext)) {
                        currentContext = newContext;
                        strategy = contextStrategies.getOrDefault(
                            currentContext,
                            strategy
                        );
                    }
                } else {
                    throw e; // Don't retry if it's a fatal exception
                }
            }
        }
        
        throw lastException;
    }
    
    private Context analyzeExecutionContext() {
        NetworkCondition networkCondition = analyzeNetworkCondition();
        BrowserState browserState = analyzeBrowserState();
        return new Context(networkCondition, browserState);
    }
    
    private NetworkCondition analyzeNetworkCondition() {
        if (driver instanceof JavascriptExecutor) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            try {
                Object timing = js.executeScript(
                    "return window.performance && window.performance.timing"
                );
                
                if (timing != null) {
                    Long responseTime = (Long) js.executeScript(
                        "return window.performance.timing.responseEnd - " +
                        "window.performance.timing.requestStart"
                    );
                    
                    if (responseTime != null) {
                        return responseTime > 1000 ? 
                            NetworkCondition.POOR : NetworkCondition.GOOD;
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to analyze network condition: {}", e.getMessage());
            }
        }
        return NetworkCondition.UNKNOWN;
    }
    
    private BrowserState analyzeBrowserState() {
        if (driver instanceof JavascriptExecutor) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            try {
                // Check for JavaScript errors
                Object errors = js.executeScript(
                    "return window.jsErrors || [];"
                );
                
                // Check memory usage if available
                Object memory = js.executeScript(
                    "return window.performance && window.performance.memory ? " +
                    "window.performance.memory.usedJSHeapSize : 0"
                );
                
                if (errors instanceof Object[] && ((Object[]) errors).length > 0) {
                    return BrowserState.UNSTABLE;
                }
                
                if (memory instanceof Long && (Long) memory > 100_000_000) { // 100MB
                    return BrowserState.UNSTABLE;
                }
                
                return BrowserState.STABLE;
            } catch (Exception e) {
                log.debug("Failed to analyze browser state: {}", e.getMessage());
            }
        }
        return BrowserState.UNKNOWN;
    }
    
    private boolean shouldContinueRetrying(Exception e) {
        // Define which exceptions are retryable
        return !(e instanceof IllegalArgumentException) && 
               !(e instanceof IllegalStateException);
    }
    
    public void addStrategy(Context context, RetryStrategy strategy) {
        contextStrategies.put(context, strategy);
    }
    
    public static class Context {
        private final NetworkCondition networkCondition;
        private final BrowserState browserState;
        
        public Context(NetworkCondition networkCondition, BrowserState browserState) {
            this.networkCondition = networkCondition;
            this.browserState = browserState;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Context context = (Context) o;
            return networkCondition == context.networkCondition &&
                   browserState == context.browserState;
        }
        
        @Override
        public int hashCode() {
            return 31 * networkCondition.hashCode() + browserState.hashCode();
        }
    }
    
    public static class RetryStrategy {
        private final int maxRetries;
        private final long delayMs;
        
        public RetryStrategy(int maxRetries, long delayMs) {
            this.maxRetries = maxRetries;
            this.delayMs = delayMs;
        }
        
        public int getMaxRetries() {
            return maxRetries;
        }
        
        public long getDelayMs() {
            return delayMs;
        }
    }
    
    public enum NetworkCondition {
        GOOD, POOR, UNKNOWN
    }
    
    public enum BrowserState {
        STABLE, UNSTABLE, UNKNOWN
    }
}