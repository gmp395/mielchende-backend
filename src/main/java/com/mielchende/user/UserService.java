package com.mielchende.user;

import com.mielchende.user.dto.RegisterRequestDto;
import com.mielchende.user.dto.UserResponseDto;

/* Contrato del servicio de usuarios: qué hace, no cómo lo hace */
public interface UserService {

    UserResponseDto register(RegisterRequestDto request);
}