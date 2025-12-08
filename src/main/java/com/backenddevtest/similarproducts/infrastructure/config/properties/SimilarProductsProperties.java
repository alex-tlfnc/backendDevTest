package com.backenddevtest.similarproducts.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "similar-products")
public class SimilarProductsProperties {

    private long productTimeoutMs;
    private long globalTimeoutMs;
    private int productConcurrency;

    public long getProductTimeoutMs() {
        return productTimeoutMs;
    }

    public void setProductTimeoutMs(long productTimeoutMs) {
        this.productTimeoutMs = productTimeoutMs;
    }

    public long getGlobalTimeoutMs() {
        return globalTimeoutMs;
    }

    public void setGlobalTimeoutMs(long globalTimeoutMs) {
        this.globalTimeoutMs = globalTimeoutMs;
    }

    public int getProductConcurrency() {
        return productConcurrency;
    }

    public void setProductConcurrency(int productConcurrency) {
        this.productConcurrency = productConcurrency;
    }
}
