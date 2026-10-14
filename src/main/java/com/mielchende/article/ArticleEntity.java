package com.mielchende.article;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/* Artículo de la sección Actualidad. Lo escribe el admin desde el panel
   y solo lo ve el público cuando está publicado */
@Entity
@Table(name = "articles")
@Getter
@Setter
@NoArgsConstructor /* Constructor vacío obligatorio para Hibernate */
@AllArgsConstructor
@Builder
public class ArticleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Título visible en Actualidad */
    @Column(nullable = false)
    private String title;

    /* Entradilla breve que se muestra antes del texto.
       length 500: más que el VARCHAR(255) por defecto, pero acotado */
    @Column(nullable = false, length = 500)
    private String summary;

    /* Texto completo. Se guarda como texto plano con saltos de línea:
       el frontend lo muestra con {{ }}, nunca con v-html (riesgo XSS) */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    /* Ruta o URL de la imagen (opcional) */
    private String imageUrl;

    /* Borrador o publicado. Se guarda como texto, igual que en productos */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArticleStatus status;

    /* Fecha de creación. La asigna el Service, no el admin */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /* Fecha de publicación. null mientras el artículo es un borrador;
       el Service la rellena al pasarlo a PUBLISHED */
    private LocalDateTime publishedAt;
}