package com.backenddevtest.similarproducts.infrastructure.adapter.out;

import com.backenddevtest.similarproducts.application.port.out.ExistingApisClient;
import com.backenddevtest.similarproducts.domain.exception.ExternalServiceUnavailableException;
import com.backenddevtest.similarproducts.infrastructure.config.properties.ExistingApisProperties;
import com.backenddevtest.similarproducts.domain.exception.ProductNotFoundException;
import com.backenddevtest.similarproducts.domain.model.ProductDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Component
public class ExistingApisClientImpl implements ExistingApisClient {

    private final WebClient webClient;
    private final ExistingApisProperties properties;

    private static final Logger log = LoggerFactory.getLogger(ExistingApisClientImpl.class);

    public ExistingApisClientImpl(
            WebClient existingApisWebClient,
            ExistingApisProperties properties
    ){
        this.webClient = existingApisWebClient;
        this.properties = properties;
    }

    @Override
    public Mono<ProductDetail> getProductDetail(String productId) {
        return webClient.get()
                .uri(properties.getProductDetailUrl(), productId)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals,
                        clientResponse -> {
                                        log.warn("Product not found: {}", productId);
                                        return Mono.error(new ProductNotFoundException(productId));
                        })
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> {
                                         log.error("External service error for productId {}: status={}", productId, clientResponse.statusCode());
                                         return Mono.error(new ExternalServiceUnavailableException());
                        })
                .bodyToMono(ProductDetail.class)
                .retryWhen(Retry.fixedDelay(properties.getMaxRetry(), Duration.ofSeconds(properties.getDelayRetry()))
                        .filter(throwable -> throwable instanceof ExternalServiceUnavailableException)
                        .doBeforeRetry(
                                retrySignal ->
                                        log.info("Retrying getProductDetail for productId {} (attempt {})", productId, retrySignal.totalRetries() + 1)
                        )
                );
    }

    @Override
    public Mono<List<String>> getSimilarProductIds(String productId) {
        return webClient.get()
                .uri(properties.getSimilarIdsUrl(), productId)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals,
                        clientResponse -> {
                            log.warn("Product not found when fetching similar IDs: {}", productId);
                            return Mono.error(new ProductNotFoundException(productId));
                        })
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> {
                            log.error("External service error for similar products of {}: status={}", productId, clientResponse.statusCode());
                            return Mono.error(new ExternalServiceUnavailableException());
                        })
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .retryWhen(Retry.fixedDelay(properties.getMaxRetry(), Duration.ofSeconds(properties.getDelayRetry()))
                        .filter(throwable -> throwable instanceof ExternalServiceUnavailableException)
                        .doBeforeRetry(retrySignal ->
                                log.info("Retrying getSimilarProductIds for productId {} (attempt {})", productId, retrySignal.totalRetries() + 1)
                        )
                );
    }

}
