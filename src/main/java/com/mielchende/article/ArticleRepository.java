package com.mielchende.article;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/* Acceso a la tabla articles.
   Los dos query methods generan la consulta a partir de su nombre */
public interface ArticleRepository extends JpaRepository<ArticleEntity, Long> {

    /* Público: solo los artículos con un estado concreto (PUBLISHED),
       del más reciente al más antiguo según la fecha de publicación */
    List<ArticleEntity> findByStatusOrderByPublishedAtDesc(ArticleStatus status);

    /* Panel: todos, borradores incluidos, del más reciente al más antiguo */
    List<ArticleEntity> findAllByOrderByCreatedAtDesc();
}