package com.mielchende.contact;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mielchende.contact.dto.ContactMessageRequestDto;
import com.mielchende.contact.dto.ContactMessageResponseDto;

/* Lógica de los mensajes de contacto:
   guardar los que llegan del formulario y listarlos para el admin */
@Service
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    /* Inyección por constructor (apuntes, sección 6) */
    public ContactMessageServiceImpl(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    /* Guardar: la fecha de envío la pone el servidor,
       para que nadie pueda enviar una fecha inventada */
    @Override
    @Transactional
    public void create(ContactMessageRequestDto request) {
        ContactMessageEntity contactMessage = ContactMessageMapper.toEntity(request);
        contactMessage.setCreatedAt(LocalDateTime.now());
        contactMessageRepository.save(contactMessage);
    }

    /* Todos, del más reciente al más antiguo */
    @Override
    public List<ContactMessageResponseDto> findAll() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ContactMessageMapper::toResponseDto)
                .toList();
    }
}