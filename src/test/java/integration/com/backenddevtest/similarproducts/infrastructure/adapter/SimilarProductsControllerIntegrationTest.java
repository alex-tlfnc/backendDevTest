package com.backenddevtest.similarproducts.infrastructure.adapter;

import com.backenddevtest.similarproducts.application.service.SimilarProductsService;
import com.backenddevtest.similarproducts.domain.exception.ProductNotFoundException;
import com.backenddevtest.similarproducts.domain.model.ProductDetail;
import com.backenddevtest.similarproducts.infrastructure.adapter.in.SimilarProductsController;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SimilarProductsController.class)
class SimilarProductsControllerIntegrationTest {

    @Mock
    private SimilarProductsService similarProductsService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        this.webTestClient = WebTestClient.bindToController(new SimilarProductsController(similarProductsService))
                .build();
    }

    // TODO fix testing
    @Ignore
    void getSimilarProduct_success() {
        String productId = "123";
        ProductDetail product = new ProductDetail("123", "Product 123", BigDecimal.ONE,true);

        Mockito.when(similarProductsService.getSimilarProducts(productId))
                .thenReturn(Mono.just(List.of(product)));

        webTestClient.get()
                .uri("/" + productId + "/similar")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductDetail.class)
                .hasSize(1)
                .contains(product);
    }

    @Test
    void getSimilarProduct_notFound() {
        String productId = "999";

        Mockito.when(similarProductsService.getSimilarProducts(productId))
                .thenReturn(Mono.error(new ProductNotFoundException(productId)));

        webTestClient.get()
                .uri("/" + productId + "/similar")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}

