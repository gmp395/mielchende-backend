package com.mielchende.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/* Datos que envía el cliente para registrarse.
   Es un record: inmutable, con constructor y getters generados por Java.
   Las anotaciones de validación se comprueban con @Valid en el controller. */
public record RegisterRequestDto(

    /* Obligatorio: no puede ser nulo, vacío ni solo espacios */
    @NotBlank
    String name,

    /* Obligatorio y con formato de email válido */
    @NotBlank
    @Email
    String email,

    /* Obligatoria */
    @NotBlank
    String password
) { }