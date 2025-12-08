package com.backenddevtest.similarproducts.application.service;

import com.backenddevtest.similarproducts.application.port.out.ExistingApisClient;
import com.backenddevtest.similarproducts.domain.exception.ProductNotFoundException;
import com.backenddevtest.similarproducts.infrastructure.config.properties.SimilarProductsProperties;
import com.backenddevtest.similarproducts.domain.model.ProductDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

@Service
public class SimilarProductsServiceImpl implements SimilarProductsService{

    private final ExistingApisClient existingApisClient;
    private final Duration productTimeout;
    private final Duration globalTimeout;
    private final int productConcurrency;

    private static final Logger log = LoggerFactory.getLogger(SimilarProductsServiceImpl.class);

    public SimilarProductsServiceImpl(
            ExistingApisClient existingApisClient,
            SimilarProductsProperties properties
    ) {
        this.existingApisClient = existingApisClient;
        this.productTimeout = Duration.ofMillis(properties.getProductTimeoutMs());
        this.globalTimeout = Duration.ofMillis(properties.getGlobalTimeoutMs());
        this.productConcurrency = properties.getProductConcurrency();
    }

    @Override
    public Mono<List<ProductDetail>> getSimilarProducts(String productId) {
        return existingApisClient.getSimilarProductIds(productId)
                .flatMapMany(Flux::fromIterable)
                .flatMapSequential(
                        pid -> existingApisClient.getProductDetail(pid)
                                .timeout(productTimeout)
                                .doOnError(e -> log.warn("Failed to fetch product {}: {}", pid, e.getMessage()))
                                .onErrorResume(throwable -> Mono.empty()),
                        productConcurrency
                )
                .collectList()
                .timeout(globalTimeout)
                .doOnError(e -> log.error("Global timeout or error fetching similar products for {}: {}", productId, e.getMessage()))
                .onErrorResume(throwable ->{
                    if (throwable instanceof ProductNotFoundException) {
                        return Mono.error(throwable);
                    }
                    return Mono.just(Collections.emptyList());
                });
        // TODO business: should we throw error or send empty list
    }


}
