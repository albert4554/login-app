package com.loginApp.service;

import com.loginApp.dto.JwtResponseDto;
import com.loginApp.dto.LoginRequestDto;
import com.loginApp.dto.RegisterRequestDto;
import com.loginApp.dto.UserDto;
import com.loginApp.entity.Role;
import com.loginApp.repository.UserRepository;
import com.loginApp.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.http.HttpHeaders;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserDto register(RegisterRequestDto registerRequestDto){
        if(userRepository.existsByUsername(registerRequestDto.getUsername())){
            throw new RuntimeException("El usuario existe!"); //Crear su excepcion personalizada
        }
        com.loginApp.entity.User user = com.loginApp.entity.User
                .builder()
                .username(registerRequestDto.getUsername())
                .password(passwordEncoder.encode(registerRequestDto.getPassword()))
                .build();
        Role role =  roleService.findByName("USER").orElseThrow(()-> new RuntimeException("Rol no encontrado!"));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        userRepository.save(user);

        return UserDto
                .builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles())
                .build();
    }

    public JwtResponseDto login(LoginRequestDto loginRequestDto){
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getUsername(),
                            loginRequestDto.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtUtils.generateAccessToken(loginRequestDto.getUsername());
            String refreshToken = jwtUtils.generateRefreshToken(loginRequestDto.getUsername());

            return JwtResponseDto
                    .builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Credenciales invalidas");
        }
    }

    public UserDto getLoguedUser(HttpHeaders headers){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username =  ((User) authentication.getPrincipal()).getUsername();
        com.loginApp.entity.User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return UserDto
                .builder()
                .username(user.getUsername())
                .roles(user.getRoles())
                .build();
    }
}
