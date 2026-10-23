package com.mielchende.contact;

import com.mielchende.contact.dto.ContactMessageRequestDto;
import com.mielchende.contact.dto.ContactMessageResponseDto;

/* Traduce entre ContactMessageEntity y sus DTOs.
   La fecha no se copia del DTO: la decide el Service */
public class ContactMessageMapper {

    /* Clase de utilidad: solo métodos estáticos */
    private ContactMessageMapper() { }

    /* Entidad → DTO de respuesta */
    public static ContactMessageResponseDto toResponseDto(ContactMessageEntity entity) {
        return new ContactMessageResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getMessage(),
                entity.getCreatedAt());
    }

    /* DTO de petición → entidad nueva, solo con los datos del formulario */
    public static ContactMessageEntity toEntity(ContactMessageRequestDto dto) {
        return ContactMessageEntity.builder()
                .name(dto.name())
                .email(dto.email())
                .message(dto.message())
                .build();
    }
}