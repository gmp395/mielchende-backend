package com.mielchende.product;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mielchende.product.dto.ProductResponseDto;
import com.mielchende.product.exception.ProductNotFoundException;

/* Lógica de consulta del catálogo */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /* Todos los productos, convertidos a DTO (nunca entidades) */
    @Override
    public List<ProductResponseDto> findAll() {
        return productRepository.findAll().stream()
                .map(ProductMapper::toResponseDto)
                .toList();
    }

    /* Un producto por id. Si no existe, excepción personalizada → 404 */
    @Override
    public ProductResponseDto findById(Long id) {
        return productRepository.findById(id)
                .map(ProductMapper::toResponseDto)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado"));
    }
}