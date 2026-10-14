package com.mielchende.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mielchende.article.dto.ArticleRequestDto;
import com.mielchende.article.dto.ArticleResponseDto;
import com.mielchende.article.exception.ArticleNotFoundException;

/* Test unitario del servicio de artículos: estados y fechas */
@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleServiceImpl articleService;

    @Test
    void findPublishedAsksRepositoryOnlyForPublishedArticles() {
        when(articleRepository.findByStatusOrderByPublishedAtDesc(ArticleStatus.PUBLISHED))
                .thenReturn(List.of(article(1L, ArticleStatus.PUBLISHED, LocalDateTime.now())));

        List<ArticleResponseDto> result = articleService.findPublished();

        /* El filtro por estado lo hace la consulta, no el frontend */
        assertThat(result).hasSize(1);
        verify(articleRepository).findByStatusOrderByPublishedAtDesc(ArticleStatus.PUBLISHED);
    }

    @Test
    void findByIdThrowsExceptionWhenArticleDoesNotExist() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.findById(99L))
                .isInstanceOf(ArticleNotFoundException.class);
    }

    @Test
    void createAlwaysStartsAsDraftWithCreationDate() {
        /* Given: save() devuelve el mismo artículo que recibe */
        when(articleRepository.save(any(ArticleEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        /* When */
        ArticleResponseDto result = articleService.create(request("Llega la cosecha"));

        /* Then: borrador, con fecha de creación y sin fecha de publicación */
        assertThat(result.status()).isEqualTo(ArticleStatus.DRAFT);
        assertThat(result.createdAt()).isNotNull();
        assertThat(result.publishedAt()).isNull();
        assertThat(result.title()).isEqualTo("Llega la cosecha");
    }

    @Test
    void updateChangesTextsButKeepsStatusAndDates() {
        /* Given: artículo ya publicado */
        LocalDateTime publishedAt = LocalDateTime.of(2026, 10, 1, 10, 0);
        ArticleEntity existing = article(1L, ArticleStatus.PUBLISHED, publishedAt);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(articleRepository.save(existing)).thenReturn(existing);

        /* When: se corrige el título */
        ArticleResponseDto result = articleService.update(1L, request("Título corregido"));

        /* Then: cambia el texto, pero sigue publicado con la misma fecha */
        assertThat(result.title()).isEqualTo("Título corregido");
        assertThat(result.status()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(result.publishedAt()).isEqualTo(publishedAt);
    }

    @Test
    void updateThrowsExceptionWhenArticleDoesNotExist() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.update(99L, request("Título")))
                .isInstanceOf(ArticleNotFoundException.class);
        verify(articleRepository, never()).save(any());
    }

    @Test
    void publishingSetsPublicationDate() {
        ArticleEntity draft = article(1L, ArticleStatus.DRAFT, null);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(draft));
        when(articleRepository.save(draft)).thenReturn(draft);

        ArticleResponseDto result = articleService.updateStatus(1L, ArticleStatus.PUBLISHED);

        assertThat(result.status()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(result.publishedAt()).isNotNull();
    }

    @Test
    void unpublishingClearsPublicationDate() {
        ArticleEntity published = article(1L, ArticleStatus.PUBLISHED, LocalDateTime.now());
        when(articleRepository.findById(1L)).thenReturn(Optional.of(published));
        when(articleRepository.save(published)).thenReturn(published);

        ArticleResponseDto result = articleService.updateStatus(1L, ArticleStatus.DRAFT);

        assertThat(result.status()).isEqualTo(ArticleStatus.DRAFT);
        assertThat(result.publishedAt()).isNull();
    }

    @Test
    void sameStatusChangesNothing() {
        /* Given: ya publicado en una fecha concreta */
        LocalDateTime publishedAt = LocalDateTime.of(2026, 10, 1, 10, 0);
        ArticleEntity published = article(1L, ArticleStatus.PUBLISHED, publishedAt);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(published));

        /* When: se vuelve a publicar */
        ArticleResponseDto result = articleService.updateStatus(1L, ArticleStatus.PUBLISHED);

        /* Then: conserva la fecha original y no se guarda nada (idempotente) */
        assertThat(result.publishedAt()).isEqualTo(publishedAt);
        verify(articleRepository, never()).save(any());
    }

    /* Ayudantes para crear datos de prueba */
    private ArticleEntity article(Long id, ArticleStatus status, LocalDateTime publishedAt) {
        return ArticleEntity.builder()
                .id(id)
                .title("Título de prueba")
                .summary("Resumen de prueba")
                .body("Texto de prueba")
                .status(status)
                .createdAt(LocalDateTime.of(2026, 9, 30, 9, 0))
                .publishedAt(publishedAt)
                .build();
    }

    private ArticleRequestDto request(String title) {
        return new ArticleRequestDto(title, "Resumen de prueba", "Texto de prueba", null);
    }
}