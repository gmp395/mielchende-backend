package com.mielchende.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mielchende.role.RoleDataInitializer;
import com.mielchende.role.RoleEntity;
import com.mielchende.role.RoleRepository;
import com.mielchende.security.facade.PasswordEncoderFacade;
import com.mielchende.user.dto.RegisterRequestDto;
import com.mielchende.user.dto.UserResponseDto;
import com.mielchende.user.exception.UserAlreadyExistsException;

/* Implementación del servicio de usuarios: aquí vive la lógica del registro */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoderFacade passwordEncoderFacade;

    /* Inyección por constructor de las tres dependencias */
    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoderFacade passwordEncoderFacade) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoderFacade = passwordEncoderFacade;
    }

    /* @Transactional: si algo falla a mitad, se deshace todo (rollback);
       si todo va bien, se confirma (commit). Apuntes 15.19 */
    @Override
    @Transactional
    public UserResponseDto register(RegisterRequestDto request) {

        /* Normalizamos el email (sin espacios y en minúsculas)
           para que "Ana@Mail.com" y "ana@mail.com" cuenten como el mismo */
        String email = request.email().trim().toLowerCase();

        /* 1. El email no puede estar ya registrado */
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Este email ya está registrado");
        }

        /* 2. Rol por defecto, de mínimos privilegios.
           Si no existiera sería un fallo de configuración, no del usuario */
        RoleEntity userRole = roleRepository.findByName(RoleDataInitializer.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("ROLE_USER no existe"));

        /* 3 y 4. Construimos la entidad con el Builder,
           con la contraseña ya cifrada, y la guardamos */
        UserEntity user = UserEntity.builder()
                .name(request.name().trim())
                .email(email)
                .password(passwordEncoderFacade.encode(request.password()))
                .role(userRole)
                .build();

        UserEntity saved = userRepository.save(user);

        /* 5. Devolvemos el DTO, nunca la entidad */
        return UserMapper.toResponseDto(saved);
    }
}