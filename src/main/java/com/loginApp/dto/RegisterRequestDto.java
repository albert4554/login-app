package com.loginApp.dto;

import com.loginApp.entity.Role;
import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequestDto {
    private String username;
    private String password;
    private Set<String> roles; // Opcional: para asignar roles al usuario
}
