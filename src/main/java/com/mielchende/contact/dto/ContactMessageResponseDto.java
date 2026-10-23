package com.mielchende.contact.dto;

import java.time.LocalDateTime;

/* Datos de un mensaje que devuelve la API al panel de admin */
public record ContactMessageResponseDto(
    Long id,
    String name,
    String email,
    String message,
    LocalDateTime createdAt
) { }