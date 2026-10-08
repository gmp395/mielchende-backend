package com.mielchende.product;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mielchende.product.dto.ProductResponseDto;

/* Endpoints públicos del catálogo (sin token, según SecurityConfig) */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /* GET /api/products → catálogo completo (200) */
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    /* GET /api/products/{id} → ficha de un producto (200), o 404 si no existe.
       @PathVariable extrae el id de la propia URL */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }
}