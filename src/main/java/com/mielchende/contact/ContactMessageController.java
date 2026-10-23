package com.mielchende.contact;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mielchende.contact.dto.ContactMessageRequestDto;

import jakarta.validation.Valid;

/* Endpoint público del formulario de Contacto (sin token, según SecurityConfig) */
@RestController
@RequestMapping("/api/contact")
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    public ContactMessageController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    /* POST → guardar el mensaje (201 Created, sin cuerpo).
       @Valid aplica las validaciones del DTO: si fallan, 400 */
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody ContactMessageRequestDto request) {
        contactMessageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}