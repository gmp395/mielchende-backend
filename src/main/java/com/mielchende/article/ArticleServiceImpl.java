package com.mielchende.article;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mielchende.article.dto.ArticleRequestDto;
import com.mielchende.article.dto.ArticleResponseDto;
import com.mielchende.article.exception.ArticleNotFoundException;

/* Lógica de los artículos de Actualidad:
   consulta pública (solo publicados) y gestión por el admin */
@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleServiceImpl(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    /* Solo los publicados, del más reciente al más antiguo */
    @Override
    public List<ArticleResponseDto> findPublished() {
        return articleRepository.findByStatusOrderByPublishedAtDesc(ArticleStatus.PUBLISHED).stream()
                .map(ArticleMapper::toResponseDto)
                .toList();
    }

    /* Todos, borradores incluidos, para el panel */
    @Override
    public List<ArticleResponseDto> findAll() {
        return articleRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ArticleMapper::toResponseDto)
                .toList();
    }

    /* Un artículo por id (panel), o 404 si no existe */
    @Override
    public ArticleResponseDto findById(Long id) {
        return ArticleMapper.toResponseDto(getArticleOrThrow(id));
    }

    /* Crear: siempre nace como borrador, con la fecha de creación actual
       y sin fecha de publicación */
    @Override
    @Transactional
    public ArticleResponseDto create(ArticleRequestDto request) {
        ArticleEntity article = ArticleMapper.toEntity(request);
        article.setStatus(ArticleStatus.DRAFT);
        article.setCreatedAt(LocalDateTime.now());
        article.setPublishedAt(null);
        return ArticleMapper.toResponseDto(articleRepository.save(article));
    }

    /* Editar los textos. El estado y las fechas no cambian */
    @Override
    @Transactional
    public ArticleResponseDto update(Long id, ArticleRequestDto request) {
        ArticleEntity article = getArticleOrThrow(id);
        ArticleMapper.updateEntity(article, request);
        return ArticleMapper.toResponseDto(articleRepository.save(article));
    }

    /* Publicar o despublicar.
       - Mismo estado: no se hace nada (operación idempotente).
       - A PUBLISHED: se registra la fecha de publicación.
       - A DRAFT: se borra la fecha; si se republica, tendrá la nueva */
    @Override
    @Transactional
    public ArticleResponseDto updateStatus(Long id, ArticleStatus status) {
        ArticleEntity article = getArticleOrThrow(id);

        if (article.getStatus() == status) {
            return ArticleMapper.toResponseDto(article);
        }

        article.setStatus(status);
        article.setPublishedAt(status == ArticleStatus.PUBLISHED ? LocalDateTime.now() : null);
        return ArticleMapper.toResponseDto(articleRepository.save(article));
    }

    /* Método privado reutilizado: busca el artículo o lanza la excepción.
       Así el mensaje y el comportamiento del 404 están en un solo sitio */
    private ArticleEntity getArticleOrThrow(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException("Artículo no encontrado"));
    }
}