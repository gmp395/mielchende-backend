package com.mielchende.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/* Datos que envía el visitante desde el formulario de Contacto.
   Los límites coinciden con las columnas: así un texto demasiado largo
   da 400 con un mensaje claro, en vez de un error de la base de datos */
public record ContactMessageRequestDto(

    @NotBlank
    @Size(max = 100)
    String name,

    /* @Email comprueba que tenga formato de correo */
    @NotBlank
    @Email
    @Size(max = 255)
    String email,

    /* 2000 caracteres: de sobra para una consulta,
       y evita que se envíen textos enormes */
    @NotBlank
    @Size(max = 2000)
    String message,

    /* Token del captcha que entrega Cloudflare al superarlo.
       No se guarda: solo se verifica en el Service */
    @NotBlank
    String captchaToken
) { }