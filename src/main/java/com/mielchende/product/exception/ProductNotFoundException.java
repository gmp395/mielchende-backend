package com.mielchende.product.exception;

/* Se lanza al buscar un producto por un id que no existe */
public class ProductNotFoundException extends ProductException {

    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}