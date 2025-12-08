package com.backenddevtest.similarproducts.application.service;

import com.backenddevtest.similarproducts.domain.model.ProductDetail;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SimilarProductsService {

    Mono<List<ProductDetail>> getSimilarProducts(String productId);
}
