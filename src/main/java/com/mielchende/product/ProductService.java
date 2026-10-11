package com.mielchende.product;

import java.util.List;

import com.mielchende.product.dto.ProductRequestDto;
import com.mielchende.product.dto.ProductResponseDto;

/* Contrato del servicio de productos */
public interface ProductService {

    List<ProductResponseDto> findAll();

    ProductResponseDto findById(Long id);

    ProductResponseDto create(ProductRequestDto request);

    ProductResponseDto update(Long id, ProductRequestDto request);

    ProductResponseDto updateStatus(Long id, ProductStatus status);

    void delete(Long id);
}