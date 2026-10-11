package com.mielchende.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mielchende.role.RoleDataInitializer;
import com.mielchende.role.RoleEntity;
import com.mielchende.role.RoleRepository;
import com.mielchende.security.facade.PasswordEncoderFacade;

/* Test unitario de la creación de la cuenta de administrador */
@ExtendWith(MockitoExtension.class)
class AdminUserInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoderFacade passwordEncoderFacade;

    /* Aquí no usamos @InjectMocks porque el constructor también recibe textos
       (nombre, email, contraseña), y Mockito no sabría qué valores darles */
    private AdminUserInitializer initializer(String email, String password) {
        return new AdminUserInitializer(userRepository, roleRepository, passwordEncoderFacade,
                "José Antonio", email, password);
    }

    @Test
    void createsAdminWithEncodedPasswordAndAdminRole() {
        /* Given: el admin no existe todavía */
        when(userRepository.existsByEmail("admin@mail.com")).thenReturn(false);
        when(roleRepository.findByName(RoleDataInitializer.ROLE_ADMIN))
                .thenReturn(Optional.of(RoleEntity.builder().name(RoleDataInitializer.ROLE_ADMIN).build()));
        when(passwordEncoderFacade.encode("secreta")).thenReturn("hash-bcrypt");

        /* When: email con mayúsculas, como podría escribirse en el .env */
        initializer("Admin@Mail.com", "secreta").run();

        /* Then: se guarda con email normalizado, contraseña cifrada y ROLE_ADMIN */
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("admin@mail.com");
        assertThat(saved.getPassword()).isEqualTo("hash-bcrypt");
        assertThat(saved.getRole().getName()).isEqualTo(RoleDataInitializer.ROLE_ADMIN);
    }

    @Test
    void doesNothingWhenAdminAlreadyExists() {
        when(userRepository.existsByEmail("admin@mail.com")).thenReturn(true);

        initializer("admin@mail.com", "secreta").run();

        verify(userRepository, never()).save(any());
    }

    @Test
    void doesNothingWhenNotConfigured() {
        /* Sin datos en el .env: no se crea nada y la aplicación sigue */
        initializer("", "").run();

        verify(userRepository, never()).save(any());
    }
}