package com.mielchende.role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/* Test unitario: se prueba RoleDataInitializer sin base de datos.
   MockitoExtension activa las anotaciones @Mock e @InjectMocks. */
@ExtendWith(MockitoExtension.class)
class RoleDataInitializerTest {

    /* Repositorio falso: le decimos qué responder en cada test */
    @Mock
    private RoleRepository roleRepository;

    /* Clase real que probamos, con el mock inyectado en su constructor */
    @InjectMocks
    private RoleDataInitializer roleDataInitializer;

    @Test
    void createsBothRolesWhenTheyDoNotExist() {
        /* Given: ningún rol existe en la base de datos */
        when(roleRepository.findByName(any())).thenReturn(Optional.empty());

        /* When: se ejecuta el inicializador */
        roleDataInitializer.run();

        /* Then: se guardan dos roles, y son ROLE_USER y ROLE_ADMIN.
           ArgumentCaptor "atrapa" los objetos que se pasaron a save() */
        ArgumentCaptor<RoleEntity> captor = ArgumentCaptor.forClass(RoleEntity.class);
        verify(roleRepository, times(2)).save(captor.capture());

        List<String> savedNames = captor.getAllValues().stream()
                .map(RoleEntity::getName)
                .toList();
        assertThat(savedNames).containsExactlyInAnyOrder(
                RoleDataInitializer.ROLE_USER,
                RoleDataInitializer.ROLE_ADMIN);
    }

    @Test
    void doesNotCreateRolesWhenTheyAlreadyExist() {
        /* Given: los dos roles ya existen */
        when(roleRepository.findByName(any()))
                .thenReturn(Optional.of(new RoleEntity()));

        /* When */
        roleDataInitializer.run();

        /* Then: no se guarda nada, así que no hay duplicados */
        verify(roleRepository, never()).save(any());
    }
}