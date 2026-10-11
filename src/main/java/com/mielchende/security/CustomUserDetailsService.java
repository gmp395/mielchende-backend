package com.mielchende.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mielchende.user.UserEntity;
import com.mielchende.user.UserRepository;

/* Le dice a Spring Security cómo cargar un usuario desde nuestra base de datos.
   Spring lo llama automáticamente durante el login (apuntes 15.16). */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /* El parámetro se llama "username" porque así lo define Spring,
       pero en nuestra aplicación el identificador es el email */
    @Override
    public UserDetails loadUserByUsername(String username) {

        /* Normalizamos igual que en el registro */
        String email = username.trim().toLowerCase();

        /* Si no existe, lanzamos la excepción que espera Spring.
           Spring responderá 401 sin decir si el fallo es el email o la contraseña,
           para no revelar qué emails están registrados */
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        /* Mapeamos UserEntity → UserDetails con los tres datos que necesita Spring */
        return User.withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole().getName())
                .build();
    }
}