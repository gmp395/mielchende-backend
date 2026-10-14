package com.mielchende.article.dto;

import com.mielchende.article.ArticleStatus;

import jakarta.validation.constraints.NotNull;

/* Cuerpo del PATCH para publicar o despublicar.
   Un valor que no sea DRAFT ni PUBLISHED da 400 (JSON no válido) */
public record ArticleStatusRequestDto(
    @NotNull
    ArticleStatus status
) { }