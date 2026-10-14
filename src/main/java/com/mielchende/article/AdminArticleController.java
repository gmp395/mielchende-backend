package com.mielchende.article;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mielchende.article.dto.ArticleRequestDto;
import com.mielchende.article.dto.ArticleResponseDto;
import com.mielchende.article.dto.ArticleStatusRequestDto;

import jakarta.validation.Valid;

/* Gestión de artículos para el admin.
   Todas las rutas /api/admin/** exigen ROLE_ADMIN (SecurityConfig).
   No hay DELETE: se despublica con el PATCH de estado */
@RestController
@RequestMapping("/api/admin/articles")
public class AdminArticleController {

    private final ArticleService articleService;

    public AdminArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /* GET → todos los artículos, borradores incluidos (200) */
    @GetMapping
    public ResponseEntity<List<ArticleResponseDto>> findAll() {
        return ResponseEntity.ok(articleService.findAll());
    }

    /* GET /{id} → un artículo, para cargar el formulario de edición (200 o 404) */
    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.findById(id));
    }

    /* POST → crear artículo como borrador (201 Created) */
    @PostMapping
    public ResponseEntity<ArticleResponseDto> create(@Valid @RequestBody ArticleRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(articleService.create(request));
    }

    /* PUT → editar los textos (200 OK) */
    @PutMapping("/{id}")
    public ResponseEntity<ArticleResponseDto> update(@PathVariable Long id,
                                                     @Valid @RequestBody ArticleRequestDto request) {
        return ResponseEntity.ok(articleService.update(id, request));
    }

    /* PATCH → publicar o despublicar (200 OK) */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ArticleResponseDto> updateStatus(@PathVariable Long id,
                                                           @Valid @RequestBody ArticleStatusRequestDto request) {
        return ResponseEntity.ok(articleService.updateStatus(id, request.status()));
    }
}