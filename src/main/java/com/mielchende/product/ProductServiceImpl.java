package com.mielchende.product;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mielchende.product.dto.ProductRequestDto;
import com.mielchende.product.dto.ProductResponseDto;
import com.mielchende.product.exception.ProductNotFoundException;

/* Lógica del catálogo: consulta pública y gestión por la admin */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /* Todos los productos, convertidos a DTO */
    @Override
    public List<ProductResponseDto> findAll() {
        return productRepository.findAll().stream()
                .map(ProductMapper::toResponseDto)
                .toList();
    }

    /* Un producto por id, o 404 si no existe */
    @Override
    public ProductResponseDto findById(Long id) {
        return ProductMapper.toResponseDto(getProductOrThrow(id));
    }

    /* Crear un producto nuevo */
    @Override
    @Transactional
    public ProductResponseDto create(ProductRequestDto request) {
        ProductEntity saved = productRepository.save(ProductMapper.toEntity(request));
        return ProductMapper.toResponseDto(saved);
    }

    /* Editar todos los datos de un producto existente */
    @Override
    @Transactional
    public ProductResponseDto update(Long id, ProductRequestDto request) {
        ProductEntity product = getProductOrThrow(id);
        ProductMapper.updateEntity(product, request);
        return ProductMapper.toResponseDto(productRepository.save(product));
    }

    /* Cambiar solo la disponibilidad (disponible / agotado) */
    @Override
    @Transactional
    public ProductResponseDto updateStatus(Long id, ProductStatus status) {
        ProductEntity product = getProductOrThrow(id);
        product.setStatus(status);
        return ProductMapper.toResponseDto(productRepository.save(product));
    }

    /* Eliminar un producto. Si no existe, 404 */
    @Override
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Producto no encontrado");
        }
        productRepository.deleteById(id);
    }

    /* Método privado reutilizado: busca el producto o lanza la excepción.
       Así el mensaje y el comportamiento del 404 están en un solo sitio */
    private ProductEntity getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado"));
    }
}