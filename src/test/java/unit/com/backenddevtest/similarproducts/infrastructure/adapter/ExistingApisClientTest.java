package com.backenddevtest.similarproducts.infrastructure.adapter;

import com.backenddevtest.similarproducts.domain.exception.ExternalServiceUnavailableException;
import com.backenddevtest.similarproducts.domain.exception.ProductNotFoundException;
import com.backenddevtest.similarproducts.infrastructure.adapter.out.ExistingApisClientImpl;
import com.backenddevtest.similarproducts.infrastructure.config.properties.ExistingApisProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ExistingApisClientTest {

    @Mock
    private ExchangeFunction exchangeFunction;

    private WebClient webClient;
    private ExistingApisClientImpl client;

    @BeforeEach
    void setup() {
        webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        ExistingApisProperties properties = new ExistingApisProperties();
        properties.setProductDetailUrl("/product/{id}");
        properties.setSimilarIdsUrl("/product/{id}/similarids");
        properties.setMaxRetry(3);
        properties.setDelayRetry(1);

        client = new ExistingApisClientImpl(webClient, properties);
    }

    private ClientResponse jsonResponse(HttpStatus status, String body) {
        return ClientResponse
                .create(status)
                .header("Content-Type", "application/json")
                .body(body)
                .build();
    }

    @Test
    void getProductDetail_success() {
        String json = """
        {"id":"P1","name":"Prod1"}""";

        Mockito.when(exchangeFunction.exchange(Mockito.any()))
                .thenReturn(Mono.just(jsonResponse(HttpStatus.OK, json)));

        StepVerifier.create(client.getProductDetail("P1"))
                .expectNextMatches(p -> p.getId().equals("P1"))
                .verifyComplete();
    }

    @Test
    void getProductDetail_404_notFound() {
        Mockito.when(exchangeFunction.exchange(Mockito.any()))
                .thenReturn(Mono.just(jsonResponse(HttpStatus.NOT_FOUND, "")));

        StepVerifier.create(client.getProductDetail("P404"))
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    @Test
    void getProductDetail_5xx_withRetryThenFail() {

        Mockito.when(exchangeFunction.exchange(Mockito.any()))
                .thenReturn(
                        Mono.just(jsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, "")),
                        Mono.just(jsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, "")),
                        Mono.just(jsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, ""))
                );
        StepVerifier.create(client.getProductDetail("PERR"))
                .expectErrorMatches(throwable ->
                        throwable.getClass().getName().equals("reactor.core.Exceptions$RetryExhaustedException") &&
                                throwable.getCause() instanceof ExternalServiceUnavailableException
                )
                .verify();

        Mockito.verify(exchangeFunction, Mockito.times(4)).exchange(Mockito.any());
    }

    @Test
    void getSimilarProductIds_success() {
        String json = """
        ["P2","P3"]""";

        Mockito.when(exchangeFunction.exchange(Mockito.any()))
                .thenReturn(Mono.just(jsonResponse(HttpStatus.OK, json)));

        StepVerifier.create(client.getSimilarProductIds("P1"))
                .expectNext(List.of("P2", "P3"))
                .verifyComplete();
    }

    @Test
    void getSimilarProductIds_404() {
        Mockito.when(exchangeFunction.exchange(Mockito.any()))
                .thenReturn(Mono.just(jsonResponse(HttpStatus.NOT_FOUND, "")));

        StepVerifier.create(client.getSimilarProductIds("PX"))
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    @Test
    void getSimilarProductIds_5xx_withRetryThenFail() {

        Mockito.when(exchangeFunction.exchange(Mockito.any()))
                .thenReturn(
                        Mono.just(jsonResponse(HttpStatus.BAD_GATEWAY, "")),
                        Mono.just(jsonResponse(HttpStatus.BAD_GATEWAY, "")),
                        Mono.just(jsonResponse(HttpStatus.BAD_GATEWAY, ""))
                );

        StepVerifier.create(client.getSimilarProductIds("PX"))
                .expectErrorMatches(throwable ->
                        throwable.getClass().getName().equals("reactor.core.Exceptions$RetryExhaustedException") &&
                                throwable.getCause() instanceof ExternalServiceUnavailableException
                )
                .verify();

        Mockito.verify(exchangeFunction, Mockito.times(4)).exchange(Mockito.any());
    }


}
