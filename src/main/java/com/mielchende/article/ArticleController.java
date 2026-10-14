package com.mielchende.article;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mielchende.article.dto.ArticleResponseDto;

/* Endpoint público de Actualidad (sin token, según SecurityConfig) */
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /* GET /api/articles → solo los artículos publicados (200).
       Los borradores nunca salen por esta ruta */
    @GetMapping
    public ResponseEntity<List<ArticleResponseDto>> findPublished() {
        return ResponseEntity.ok(articleService.findPublished());
    }
}