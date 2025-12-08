package com.backenddevtest.similarproducts.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "external.existing-apis")
public class ExistingApisProperties {
    private String baseUrl;
    private long timeoutMs;
    private String similarIdsUrl;
    private String productDetailUrl;
    private int maxConnections;
    private int maxRetry;
    private int delayRetry;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public String getSimilarIdsUrl() {
        return similarIdsUrl;
    }

    public void setSimilarIdsUrl(String similarIdsUrl) {
        this.similarIdsUrl = similarIdsUrl;
    }

    public String getProductDetailUrl() {
        return productDetailUrl;
    }

    public void setProductDetailUrl(String productDetailUrl) {
        this.productDetailUrl = productDetailUrl;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public int getDelayRetry() {
        return delayRetry;
    }

    public void setDelayRetry(int delayRetry) {
        this.delayRetry = delayRetry;
    }
}
