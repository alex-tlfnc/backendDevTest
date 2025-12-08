package com.backenddevtest.similarproducts.infrastructure.adapter.in;

import com.backenddevtest.similarproducts.domain.model.ProductDetail;
import com.backenddevtest.similarproducts.application.service.SimilarProductsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/product")
public class SimilarProductsController {

    private final SimilarProductsService similarProductsService;

    public SimilarProductsController(SimilarProductsService similarProductsService) {
        this.similarProductsService = similarProductsService;
    }

    @GetMapping("/{productId}/similar")
    public Mono<ResponseEntity<List<ProductDetail>>> getSimilarProduct(@PathVariable String productId) {
        return similarProductsService.getSimilarProducts(productId)
                .map(ResponseEntity::ok)
                .onErrorResume(throwable -> Mono.just(ResponseEntity.notFound().build()));
    }
}
