package com.mielchende.user;

import com.mielchende.user.dto.UserResponseDto;

/* Traduce la entidad UserEntity al DTO de respuesta.
   La contraseña nunca se copia al DTO. */
public class UserMapper {

    /* Constructor privado: es una clase de utilidad con métodos estáticos,
       no tiene sentido crear objetos de ella */
    private UserMapper() { }

    public static UserResponseDto toResponseDto(UserEntity entity) {
        return new UserResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getRole().getName());
    }
}