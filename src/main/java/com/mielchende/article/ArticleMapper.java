package com.mielchende.article;

import com.mielchende.article.dto.ArticleRequestDto;
import com.mielchende.article.dto.ArticleResponseDto;

/* Traduce entre ArticleEntity y sus DTOs.
   El estado y las fechas no se copian del DTO: los decide el Service */
public class ArticleMapper {

    /* Clase de utilidad: solo métodos estáticos */
    private ArticleMapper() { }

    /* Entidad → DTO de respuesta */
    public static ArticleResponseDto toResponseDto(ArticleEntity entity) {
        return new ArticleResponseDto(
                entity.getId(),
                entity.getTitle(),
                entity.getSummary(),
                entity.getBody(),
                entity.getImageUrl(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getPublishedAt());
    }

    /* DTO de petición → entidad nueva (para crear), solo con los textos */
    public static ArticleEntity toEntity(ArticleRequestDto dto) {
        return ArticleEntity.builder()
                .title(dto.title())
                .summary(dto.summary())
                .body(dto.body())
                .imageUrl(dto.imageUrl())
                .build();
    }

    /* Copia los textos del DTO sobre una entidad existente (para editar).
       No toca el estado ni las fechas */
    public static void updateEntity(ArticleEntity entity, ArticleRequestDto dto) {
        entity.setTitle(dto.title());
        entity.setSummary(dto.summary());
        entity.setBody(dto.body());
        entity.setImageUrl(dto.imageUrl());
    }
}