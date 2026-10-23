package com.mielchende.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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

    /* Obligatoria y con al menos 8 caracteres
       (la misma regla que PASSWORD_MIN_LENGTH en el frontend).
       Si no se cumple, el GlobalExceptionHandler responde 400
       con el mensaje "password: debe tener al menos 8 caracteres" */
    @NotBlank
    @Size(min = 8, message = "debe tener al menos 8 caracteres")
    String password,

    /* Token del captcha que entrega Cloudflare al superarlo.
       No se guarda: solo se verifica en el Service */
    @NotBlank
    String captchaToken
) { }