package com.mielchende.contact;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mielchende.contact.dto.ContactMessageRequestDto;
import com.mielchende.contact.dto.ContactMessageResponseDto;
import com.mielchende.security.captcha.CaptchaVerifier;
import com.mielchende.security.captcha.exception.InvalidCaptchaException;

/* Lógica de los mensajes de contacto:
   guardar los que llegan del formulario y listarlos para el admin */
@Service
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;
    private final CaptchaVerifier captchaVerifier;

    /* Inyección por constructor (apuntes, sección 6) */
    public ContactMessageServiceImpl(ContactMessageRepository contactMessageRepository,
                                     CaptchaVerifier captchaVerifier) {
        this.contactMessageRepository = contactMessageRepository;
        this.captchaVerifier = captchaVerifier;
    }

    /* Guardar: primero se comprueba el captcha; si no es válido, no se guarda nada.
       La fecha de envío la pone el servidor, para que nadie pueda enviar una fecha inventada */
    @Override
    @Transactional
    public void create(ContactMessageRequestDto request) {
        if (!captchaVerifier.verify(request.captchaToken())) {
            throw new InvalidCaptchaException("No se ha podido verificar que no eres un robot");
        }

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