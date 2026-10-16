package com.mielchende.article;

/* Estado de publicación de un artículo.
   En vez de borrar, el admin despublica volviendo a DRAFT:
   así no se pierde el texto y el público deja de verlo */
public enum ArticleStatus {
    DRAFT,
    PUBLISHED
}