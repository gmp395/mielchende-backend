package com.mielchende.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mielchende.security.TokenService;
import com.mielchende.user.UserService;
import com.mielchende.user.dto.RegisterRequestDto;
import com.mielchende.user.dto.UserResponseDto;

import jakarta.validation.Valid;

/* Endpoints de autenticación: registro y login. Ruta base: /api/auth */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;

    public AuthController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    /* POST /api/auth/register → 201 con los datos del usuario creado */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        UserResponseDto response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /* POST /api/auth/login con Basic Auth (email y contraseña en la cabecera).
       Cuando el código llega aquí, Spring Security YA ha comprobado las credenciales:
       si eran incorrectas, habría respondido 401 sin entrar en este método.
       Spring nos pasa el usuario autenticado en el parámetro authentication */
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(Authentication authentication) {
        String token = tokenService.generateToken(authentication);
        return ResponseEntity.ok(new TokenResponseDto(token));
    }
}