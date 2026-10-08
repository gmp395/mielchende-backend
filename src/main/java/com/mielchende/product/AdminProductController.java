package com.mielchende.product;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mielchende.product.dto.ProductRequestDto;
import com.mielchende.product.dto.ProductResponseDto;
import com.mielchende.product.dto.ProductStatusRequestDto;

import jakarta.validation.Valid;

/* Gestión de productos para la administradora.
   Todas las rutas /api/admin/** exigen ROLE_ADMIN (SecurityConfig) */
@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    /* POST → crear producto (201 Created) */
    @PostMapping
    public ResponseEntity<ProductResponseDto> create(@Valid @RequestBody ProductRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    /* PUT → editar todos los datos (200 OK) */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> update(@PathVariable Long id,
                                                     @Valid @RequestBody ProductRequestDto request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    /* PATCH → cambiar solo la disponibilidad (200 OK) */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductResponseDto> updateStatus(@PathVariable Long id,
                                                           @Valid @RequestBody ProductStatusRequestDto request) {
        return ResponseEntity.ok(productService.updateStatus(id, request.status()));
    }

    /* DELETE → eliminar (204 No Content: éxito sin cuerpo) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}