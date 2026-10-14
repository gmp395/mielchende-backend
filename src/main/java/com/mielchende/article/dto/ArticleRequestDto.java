package com.mielchende.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/* Datos que envía el admin para crear o editar un artículo.
   El estado no va aquí: todo artículo nace como borrador
   y se publica con el PATCH de estado. imageUrl es opcional */
public record ArticleRequestDto(

    /* Máximo 255: el tamaño por defecto de la columna */
    @NotBlank
    @Size(max = 255)
    String title,

    /* Máximo 500, igual que la columna. Así un texto largo da 400
       con un mensaje claro, en vez de un error de la base de datos */
    @NotBlank
    @Size(max = 500)
    String summary,

    @NotBlank
    String body,

    String imageUrl
) { }