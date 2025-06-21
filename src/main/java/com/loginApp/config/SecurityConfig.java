package com.loginApp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loginApp.dto.ErrorResponse;
import com.loginApp.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)//se desabilita para que nos permita, insertar, editar, eliminar
                .cors(Customizer.withDefaults())//habilitar la peticion cruzada
                .sessionManagement(sess->sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //Cualquier peticion debe estar autenticada
                .authorizeHttpRequests(//esto para que me autorize las peticiones http
                        customizeRequests -> customizeRequests //reglas sobre que va hacer con esas peticiones http
                                //reglas que permiten denegar o permitir el acceso a diferentes endpoints
                                .requestMatchers("/api/auth/**").permitAll()  // Rutas de autenticación accesibles sin autenticación
                                .anyRequest()//cualquier peticion se necesita esta autenticado
                                .authenticated())

                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint(customAuthenticationEntryPoint())
                                .accessDeniedHandler(customAccessDeniedHandler())
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    //Creamos este bean para que lo podamos inyectar en el controlador
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager(); //utiliza el authentication manager por defecto
    }
    //usamos el password encoder porque lo usamos en el userDetailsService
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            ErrorResponse errorResponse = new ErrorResponse(HttpServletResponse.SC_UNAUTHORIZED,"Unauthorized",authException.getMessage(),request.getServletPath());
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            String json = objectMapper.writeValueAsString(errorResponse);
            response.getWriter().write(json);
        };
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            ErrorResponse errorResponse = new ErrorResponse(HttpServletResponse.SC_FORBIDDEN,"Forbidden", accessDeniedException.getMessage(),request.getServletPath());
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            String json = objectMapper.writeValueAsString(errorResponse);
            response.getWriter().write(json);
        };
    }

    /*
    Securing GET /error
        Spring Security está protegiendo la ruta /error que es donde normalmente Spring Boot envía las
        peticiones cuando ocurre un error (por ejemplo, un 401 o 403 no manejado).
     */
}
