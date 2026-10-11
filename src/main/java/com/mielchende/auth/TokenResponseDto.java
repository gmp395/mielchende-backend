package com.mielchende.auth;

/* Respuesta del login: el token que el frontend guardará
   y enviará en cada petición como "Authorization: Bearer <token>" */
public record TokenResponseDto(String token) { }