package com.mielchende.article.exception;

/* Excepción base para los errores relacionados con artículos */
public class ArticleException extends RuntimeException {

    public ArticleException(String message) {
        super(message);
    }

    public ArticleException(String message, Throwable cause) {
        super(message, cause);
    }
}