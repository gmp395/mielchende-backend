package com.mielchende.article.exception;

/* Se lanza al buscar un artículo que no existe,
   o un borrador desde la ruta pública */
public class ArticleNotFoundException extends ArticleException {

    public ArticleNotFoundException(String message) {
        super(message);
    }

    public ArticleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}