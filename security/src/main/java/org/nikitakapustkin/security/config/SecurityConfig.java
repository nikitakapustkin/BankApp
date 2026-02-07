package org.nikitakapustkin.security.config;

import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.security.constants.SecurityApiPaths;
import org.nikitakapustkin.security.adapters.in.web.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers(
                                SecurityApiPaths.LOGIN,
                                SecurityApiPaths.USERS_REGISTER,
                                SecurityApiPaths.ROOT,
                                "/ui/**",
                                "/error"
                        )
                        .permitAll()
                        .requestMatchers(SecurityApiPaths.USERS_ME, SecurityApiPaths.USERS_ME_ALL)
                        .hasRole("CLIENT")
                        .requestMatchers(SecurityApiPaths.TRANSACTIONS_ALL)
                        .hasRole("ADMIN")
                        .requestMatchers(SecurityApiPaths.EVENTS, SecurityApiPaths.EVENTS_ALL)
                        .hasRole("ADMIN")
                        .requestMatchers(SecurityApiPaths.ACCOUNTS_ALL, SecurityApiPaths.USERS_ALL)
                        .hasRole("ADMIN")
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .logout(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
