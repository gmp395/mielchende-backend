package com.mielchende.contact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mielchende.contact.dto.ContactMessageRequestDto;
import com.mielchende.contact.dto.ContactMessageResponseDto;

/* Test unitario del servicio de mensajes de contacto:
   qué se guarda y en qué orden se devuelven */
@ExtendWith(MockitoExtension.class)
class ContactMessageServiceImplTest {

    @Mock
    private ContactMessageRepository contactMessageRepository;

    @InjectMocks
    private ContactMessageServiceImpl contactMessageService;

    @Test
    void createSavesFormDataWithCreationDate() {
        /* Given */
        ContactMessageRequestDto request = new ContactMessageRequestDto(
                "Ana", "ana@prueba.com", "¿Tenéis miel de castaño?");

        /* When */
        contactMessageService.create(request);

        /* Then: el ArgumentCaptor "atrapa" la entidad que llegó a save()
           para poder comprobar su contenido */
        ArgumentCaptor<ContactMessageEntity> captor = ArgumentCaptor.forClass(ContactMessageEntity.class);
        verify(contactMessageRepository).save(captor.capture());
        ContactMessageEntity saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("Ana");
        assertThat(saved.getEmail()).isEqualTo("ana@prueba.com");
        assertThat(saved.getMessage()).isEqualTo("¿Tenéis miel de castaño?");
        /* La fecha la pone el servidor */
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void findAllReturnsMessagesInRepositoryOrder() {
        /* Given: el repositorio ya los devuelve del más reciente al más antiguo */
        when(contactMessageRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(
                message(2L, "Reciente"),
                message(1L, "Antiguo")));

        /* When */
        List<ContactMessageResponseDto> result = contactMessageService.findAll();

        /* Then: se convierten a DTO sin cambiar el orden */
        assertThat(result).extracting(ContactMessageResponseDto::message)
                .containsExactly("Reciente", "Antiguo");
    }

    /* Ayudante: crea un mensaje con id y texto */
    private ContactMessageEntity message(Long id, String text) {
        return ContactMessageEntity.builder()
                .id(id)
                .name("Ana")
                .email("ana@prueba.com")
                .message(text)
                .createdAt(LocalDateTime.now())
                .build();
    }
}