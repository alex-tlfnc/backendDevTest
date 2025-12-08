package com.backenddevtest.similarproducts.application.port.out;

import com.backenddevtest.similarproducts.domain.model.ProductDetail;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ExistingApisClient {

    Mono<ProductDetail> getProductDetail(String productId);

    Mono<List<String>> getSimilarProductIds(String productId);
}
