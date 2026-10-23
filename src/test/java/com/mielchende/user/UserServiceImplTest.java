package com.mielchende.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mielchende.role.RoleDataInitializer;
import com.mielchende.role.RoleEntity;
import com.mielchende.role.RoleRepository;
import com.mielchende.security.captcha.CaptchaVerifier;
import com.mielchende.security.captcha.exception.InvalidCaptchaException;
import com.mielchende.security.facade.PasswordEncoderFacade;
import com.mielchende.user.dto.RegisterRequestDto;
import com.mielchende.user.dto.UserResponseDto;
import com.mielchende.user.exception.UserAlreadyExistsException;

/* Test unitario del registro: todas las dependencias son mocks */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoderFacade passwordEncoderFacade;

    @Mock
    private CaptchaVerifier captchaVerifier;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registersUserWithEncodedPasswordAndUserRole() {
        /* Given: captcha válido, email libre, el rol existe y el cifrado devuelve un hash */
        RegisterRequestDto request = new RegisterRequestDto("Ana", " Ana@Mail.com ", "secreta123", "token-valido");
        RoleEntity userRole = RoleEntity.builder().id(1L).name(RoleDataInitializer.ROLE_USER).build();

        when(captchaVerifier.verify("token-valido")).thenReturn(true);
        when(userRepository.existsByEmail("ana@mail.com")).thenReturn(false);
        when(roleRepository.findByName(RoleDataInitializer.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoderFacade.encode("secreta123")).thenReturn("hash-bcrypt");
        /* save() devuelve el mismo usuario que recibe, con un id asignado */
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        /* When */
        UserResponseDto response = userService.register(request);

        /* Then: lo que se guarda lleva el email normalizado, el hash y ROLE_USER */
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("ana@mail.com");
        assertThat(saved.getPassword()).isEqualTo("hash-bcrypt");
        assertThat(saved.getRole().getName()).isEqualTo(RoleDataInitializer.ROLE_USER);

        /* Y la respuesta trae los datos públicos del usuario */
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.email()).isEqualTo("ana@mail.com");
        assertThat(response.role()).isEqualTo(RoleDataInitializer.ROLE_USER);
    }

    @Test
    void throwsExceptionWhenEmailAlreadyExists() {
        /* Given: captcha válido, pero el email ya está registrado */
        RegisterRequestDto request = new RegisterRequestDto("Ana", "ana@mail.com", "secreta123", "token-valido");
        when(captchaVerifier.verify("token-valido")).thenReturn(true);
        when(userRepository.existsByEmail("ana@mail.com")).thenReturn(true);

        /* When + Then: se lanza la excepción y no se guarda nada */
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsInvalidCaptchaBeforeCheckingEmail() {
        /* Given: captcha no superado */
        RegisterRequestDto request = new RegisterRequestDto("Bot", "bot@spam.com", "secreta123", "token-falso");
        when(captchaVerifier.verify("token-falso")).thenReturn(false);

        /* When + Then: se rechaza sin llegar a consultar si el email existe.
           Así un bot no puede averiguar qué correos tienen cuenta */
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(InvalidCaptchaException.class);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }
}