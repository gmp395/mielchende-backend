package com.mielchende.article;

import java.util.List;

import com.mielchende.article.dto.ArticleRequestDto;
import com.mielchende.article.dto.ArticleResponseDto;

/* Contrato del servicio de artículos. Sin delete: se despublica cambiando el estado */
public interface ArticleService {

    /* Para la página pública de Actualidad */
    List<ArticleResponseDto> findPublished();

    /* Para el panel: todos los artículos */
    List<ArticleResponseDto> findAll();

    ArticleResponseDto findById(Long id);

    ArticleResponseDto create(ArticleRequestDto request);

    ArticleResponseDto update(Long id, ArticleRequestDto request);

    ArticleResponseDto updateStatus(Long id, ArticleStatus status);
}