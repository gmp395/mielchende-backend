package com.mielchende.article.dto;

import java.time.LocalDateTime;

import com.mielchende.article.ArticleStatus;

/* Datos de un artículo que devuelve la API (Actualidad y panel) */
public record ArticleResponseDto(
    Long id,
    String title,
    String summary,
    String body,
    String imageUrl,
    ArticleStatus status,
    LocalDateTime createdAt,
    LocalDateTime publishedAt
) { }