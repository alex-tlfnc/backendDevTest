package com.backenddevtest.similarproducts.application.service;

import com.backenddevtest.similarproducts.application.port.out.ExistingApisClient;
import com.backenddevtest.similarproducts.domain.exception.ExternalServiceUnavailableException;
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

public class SimilarProductsResilienceTest {

    private ExistingApisClient client;
    private SimilarProductsService service;

    @BeforeEach
    public void setup(){
        client = Mockito.mock(ExistingApisClient.class);
        SimilarProductsProperties props = new SimilarProductsProperties();
        props.setProductTimeoutMs(500);
        props.setGlobalTimeoutMs(2000);
        props.setProductConcurrency(10);

        service = new SimilarProductsServiceImpl(client, props);
    }

    @Test
    void getSimilarProducts_shouldReturnEmptyList_onGlobalTimeout() {
        String productId = "123";

        when(client.getSimilarProductIds(productId))
                .thenReturn(Mono.just(List.of("1", "2", "3")));

        when(client.getProductDetail(anyString()))
                .thenAnswer(invocation -> Mono.delay(Duration.ofMillis(3000))
                        .thenReturn(new ProductDetail(invocation.getArgument(0), "name", BigDecimal.ONE,true)));

        StepVerifier.create(service.getSimilarProducts(productId))
                .expectNext(Collections.emptyList())
                .verifyComplete();
    }

    @Test
    void getSimilarProducts_shouldHandlePartialFailures_withConcurrency() {
        String productId = "123";

        when(client.getSimilarProductIds(productId))
                .thenReturn(Mono.just(List.of("A", "B", "C")));

        when(client.getProductDetail("A"))
                .thenReturn(Mono.just(new ProductDetail("A", "Prod A", BigDecimal.ONE,true)));
        when(client.getProductDetail("B"))
                .thenReturn(Mono.error(new ExternalServiceUnavailableException()));
        when(client.getProductDetail("C"))
                .thenReturn(Mono.just(new ProductDetail("C", "Prod C", BigDecimal.ONE,true)));

        StepVerifier.create(service.getSimilarProducts(productId))
                .expectNextMatches(list ->
                        list.size() == 2 &&
                                list.stream().anyMatch(p -> p.getId().equals("A")) &&
                                list.stream().anyMatch(p -> p.getId().equals("C"))
                )
                .verifyComplete();
    }

    @Test
    void getSimilarProducts_shouldSkipProducts_whenProductTimeoutExceeded() {
        String productId = "123";

        when(client.getSimilarProductIds(productId))
                .thenReturn(Mono.just(List.of("A", "B")));

        when(client.getProductDetail("A"))
                .thenReturn(Mono.delay(Duration.ofMillis(2000)) // plus long que le timeout configuré
                        .thenReturn(new ProductDetail("A", "Prod A", BigDecimal.ONE,true)));

        when(client.getProductDetail("B"))
                .thenReturn(Mono.just(new ProductDetail("B", "Prod B", BigDecimal.ONE,true)));

        StepVerifier.create(service.getSimilarProducts(productId))
                .expectNextMatches(list ->
                        list.size() == 1 &&
                                list.get(0).getId().equals("B")
                )
                .verifyComplete();
    }


}
