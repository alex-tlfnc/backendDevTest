package com.backenddevtest.similarproducts.application.service;

import com.backenddevtest.similarproducts.application.port.out.ExistingApisClient;
import com.backenddevtest.similarproducts.domain.exception.ExternalServiceUnavailableException;
import com.backenddevtest.similarproducts.domain.exception.ProductNotFoundException;
import com.backenddevtest.similarproducts.domain.model.ProductDetail;
import com.backenddevtest.similarproducts.infrastructure.config.properties.SimilarProductsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class SimilarProductsServiceTest {

    private ExistingApisClient client;
    private SimilarProductsService service;

    @BeforeEach
    void setup() {
        client = Mockito.mock(ExistingApisClient.class);

        SimilarProductsProperties props = new SimilarProductsProperties();
        props.setProductTimeoutMs(500);
        props.setGlobalTimeoutMs(2000);
        props.setProductConcurrency(10);

        service = new SimilarProductsServiceImpl(client, props);
    }

    @Test
    void shouldReturnSortedProducts() {
        when(client.getSimilarProductIds("1"))
                .thenReturn(Mono.just(List.of("3", "1", "2")));

        when(client.getProductDetail(anyString()))
                .thenAnswer(invocation -> {
                    String id = invocation.getArgument(0);
                    return Mono.just(new ProductDetail(id, "name-" + id, BigDecimal.ONE, true));
                });

        StepVerifier.create(service.getSimilarProducts("1"))
                .expectNext(List.of(
                        new ProductDetail("3", "name-3",BigDecimal.ONE, true),
                        new ProductDetail("1", "name-1",BigDecimal.ONE, true),
                        new ProductDetail("2", "name-2",BigDecimal.ONE, true)
                ))
                .verifyComplete();
    }

    @Test
    void shouldSkipErroredProductDetail() {
        when(client.getSimilarProductIds("1"))
                .thenReturn(Mono.just(List.of("1", "2")));
        when(client.getProductDetail("1"))
                .thenReturn(Mono.error(new RuntimeException("boom")));
        when(client.getProductDetail("2"))
                .thenReturn(Mono.just(new ProductDetail("2", "ok",BigDecimal.ONE, true)));

        StepVerifier.create(service.getSimilarProducts("1"))
                .expectNext(List.of(new ProductDetail("2", "ok",BigDecimal.ONE, true)))
                .verifyComplete();
    }

    @Test
    void shouldSkipTimeout() {
        when(client.getSimilarProductIds("1"))
                .thenReturn(Mono.just(List.of("1")));

        when(client.getProductDetail("1"))
                .thenReturn(Mono.delay(Duration.ofSeconds(3)).map(i -> new ProductDetail("1", "X",BigDecimal.ONE, true)));

        StepVerifier.create(service.getSimilarProducts("1"))
                .expectNext(List.of())
                .verifyComplete();
    }

    @Test
    void shouldPropagateProductNotFound() {
        when(client.getSimilarProductIds("1"))
                .thenReturn(Mono.error(new ProductNotFoundException("1")));

        StepVerifier.create(service.getSimilarProducts("1"))
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    @Test
    void getSimilarProducts_shouldReturnEmptyList_whenExternalServiceFails() {
        String productId = "1";
        when(client.getSimilarProductIds(productId))
                .thenReturn(Mono.error(new ExternalServiceUnavailableException()));

        StepVerifier.create(service.getSimilarProducts(productId))
                .expectNext(Collections.emptyList())
                .verifyComplete();
    }

}
