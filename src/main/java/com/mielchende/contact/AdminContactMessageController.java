package com.mielchende.contact;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mielchende.contact.dto.ContactMessageResponseDto;

/* Lectura de los mensajes de contacto para el admin.
   Todas las rutas /api/admin/** exigen ROLE_ADMIN (SecurityConfig) */
@RestController
@RequestMapping("/api/admin/contact-messages")
public class AdminContactMessageController {

    private final ContactMessageService contactMessageService;

    public AdminContactMessageController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    /* GET → todos los mensajes, del más reciente al más antiguo (200) */
    @GetMapping
    public ResponseEntity<List<ContactMessageResponseDto>> findAll() {
        return ResponseEntity.ok(contactMessageService.findAll());
    }
}