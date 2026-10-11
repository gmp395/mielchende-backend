package com.mielchende.user.dto;

/* Datos del usuario que devuelve la API.
   No incluye la contraseña: nunca debe salir del backend. */
public record UserResponseDto(
    Long id,
    String name,
    String email,
    String role
) { }