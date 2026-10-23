package com.mielchende.contact;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/* Mensaje enviado desde el formulario de Contacto.
   Sustituye al correo visible en la web: el visitante deja sus datos
   y el admin los lee en el panel para responder por su cuenta */
@Entity
@Table(name = "contact_messages")
@Getter
@Setter
@NoArgsConstructor /* Constructor vacío obligatorio para Hibernate */
@AllArgsConstructor
@Builder
public class ContactMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Nombre de quien escribe. length 100: suficiente para un nombre
       y acotado, igual que valida el DTO */
    @Column(nullable = false, length = 100)
    private String name;

    /* Correo al que el admin responderá */
    @Column(nullable = false)
    private String email;

    /* Texto del mensaje. TEXT porque puede superar los 255 caracteres.
       El frontend lo mostrará con {{ }}, nunca con v-html (riesgo XSS) */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /* Fecha de envío. La asigna el Service, no el visitante */
    @Column(nullable = false)
    private LocalDateTime createdAt;
}