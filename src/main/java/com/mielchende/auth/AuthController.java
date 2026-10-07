package com.mielchende.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mielchende.user.UserService;
import com.mielchende.user.dto.RegisterRequestDto;
import com.mielchende.user.dto.UserResponseDto;

import jakarta.validation.Valid;

/* Endpoints de autenticación: registro (y más adelante, login).
   Ruta base común: /api/auth */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /* POST /api/auth/register
       @Valid: comprueba las anotaciones del DTO (@NotBlank, @Email).
       Si fallan, GlobalExceptionHandler devuelve 400.
       Si todo va bien, devuelve 201 Created con los datos del usuario */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        UserResponseDto response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}