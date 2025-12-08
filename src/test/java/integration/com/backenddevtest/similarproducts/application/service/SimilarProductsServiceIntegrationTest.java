package com.backenddevtest.similarproducts.application.service;

import com.backenddevtest.similarproducts.infrastructure.adapter.out.ExistingApisClientImpl;
import com.backenddevtest.similarproducts.infrastructure.config.properties.ExistingApisProperties;
import com.backenddevtest.similarproducts.infrastructure.config.properties.SimilarProductsProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.util.List;

class SimilarProductsServiceIntegrationTest {

    private static MockWebServer mockWebServer;
    private SimilarProductsServiceImpl service;

    @BeforeAll
    static void setupServer() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void shutdownServer() throws Exception {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        String baseUrl = mockWebServer.url("/").toString();

        var existingProps = new ExistingApisProperties();
        existingProps.setBaseUrl(baseUrl);
        existingProps.setProductDetailUrl("/product/{id}");
        existingProps.setSimilarIdsUrl("/product/{id}/similarids");
        existingProps.setTimeoutMs(1000);
        existingProps.setMaxRetry(2);
        existingProps.setDelayRetry(0);
        existingProps.setMaxConnections(10);

        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();

        ExistingApisClientImpl existingClient = new ExistingApisClientImpl(webClient, existingProps);

        var serviceProps = new SimilarProductsProperties();
        serviceProps.setProductTimeoutMs(1000);
        serviceProps.setGlobalTimeoutMs(2000);
        serviceProps.setProductConcurrency(5);

        service = new SimilarProductsServiceImpl(existingClient, serviceProps);
    }

    @Test
    void getSimilarProducts_success() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("[\"P1\",\"P2\"]")
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"id\":\"P1\",\"name\":\"Product1\"}")
                .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"id\":\"P2\",\"name\":\"Product2\"}")
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(service.getSimilarProducts("PX"))
                .expectNextMatches(list -> list.size() == 2 &&
                        list.stream().anyMatch(p -> p.getId().equals("P1")) &&
                        list.stream().anyMatch(p -> p.getId().equals("P2"))
                )
                .verifyComplete();
    }

    @Test
    void getSimilarProducts_productDetail404_skipped() {
        mockWebServer.enqueue(new MockResponse().setBody("[\"P1\"]").addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(404)); // P1 not found

        StepVerifier.create(service.getSimilarProducts("PX"))
                .expectNextMatches(List::isEmpty)
                .verifyComplete();
    }

    @Test
    void getSimilarProducts_globalTimeout() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("[\"P1\"]")
                .addHeader("Content-Type", "application/json"));

        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"id\":\"P1\",\"name\":\"Product1\"}")
                .setBodyDelay(2, java.util.concurrent.TimeUnit.SECONDS)
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(service.getSimilarProducts("PX"))
                .expectNextMatches(List::isEmpty)
                .verifyComplete();
    }

}
