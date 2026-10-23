package com.mielchende.contact;

import java.util.List;

import com.mielchende.contact.dto.ContactMessageRequestDto;
import com.mielchende.contact.dto.ContactMessageResponseDto;

/* Contrato del servicio de mensajes de contacto.
   Sin editar ni borrar: el admin solo los lee y responde por correo */
public interface ContactMessageService {

    /* Para el formulario público */
    void create(ContactMessageRequestDto request);

    /* Para el panel: todos los mensajes */
    List<ContactMessageResponseDto> findAll();
}