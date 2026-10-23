package com.mielchende.contact;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/* Acceso a la tabla contact_messages.
   JpaRepository ya aporta save(); el query method genera la consulta
   a partir de su nombre */
public interface ContactMessageRepository extends JpaRepository<ContactMessageEntity, Long> {

    /* Panel: todos los mensajes, del más reciente al más antiguo */
    List<ContactMessageEntity> findAllByOrderByCreatedAtDesc();
}