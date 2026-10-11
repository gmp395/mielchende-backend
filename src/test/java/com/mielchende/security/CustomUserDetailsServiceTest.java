package com.mielchende.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.mielchende.role.RoleDataInitializer;
import com.mielchende.role.RoleEntity;
import com.mielchende.user.UserEntity;
import com.mielchende.user.UserRepository;

/* Test unitario: comprueba la traducción de UserEntity a UserDetails */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadsUserWithEmailPasswordAndRole() {
        /* Given: existe un usuario con rol USER */
        RoleEntity role = RoleEntity.builder().name(RoleDataInitializer.ROLE_USER).build();
        UserEntity user = UserEntity.builder()
                .email("ana@mail.com")
                .password("hash-bcrypt")
                .role(role)
                .build();
        when(userRepository.findByEmail("ana@mail.com")).thenReturn(Optional.of(user));

        /* When: se busca con mayúsculas y espacios, como podría escribirlo alguien */
        UserDetails details = userDetailsService.loadUserByUsername(" Ana@Mail.com ");

        /* Then: los tres datos llegan correctamente a Spring Security */
        assertThat(details.getUsername()).isEqualTo("ana@mail.com");
        assertThat(details.getPassword()).isEqualTo("hash-bcrypt");
        assertThat(details.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(RoleDataInitializer.ROLE_USER);
    }

    @Test
    void throwsExceptionWhenUserDoesNotExist() {
        /* Given: el email no existe */
        when(userRepository.findByEmail("nadie@mail.com")).thenReturn(Optional.empty());

        /* When + Then */
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nadie@mail.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}