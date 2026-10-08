package com.mielchende.product;

import java.util.List;

import com.mielchende.product.dto.ProductResponseDto;

/* Contrato del servicio de productos */
public interface ProductService {

    List<ProductResponseDto> findAll();

    ProductResponseDto findById(Long id);
}