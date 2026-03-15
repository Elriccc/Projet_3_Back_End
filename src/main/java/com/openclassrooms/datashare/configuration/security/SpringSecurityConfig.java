package com.openclassrooms.datashare.configuration.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SpringSecurityConfig {
    private final AuthTokenFilter authenticationJwtFilter;
    private final AuthorizedPath authorizedPath;
    private final CustomUserDetailService customUserDetailService;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthTokenFilter authenticationJwtFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                //XSS protection in response header
                .headers(headers ->
                    headers.xssProtection(
                            xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                    ).contentSecurityPolicy(
                            cps -> cps.policyDirectives("script-src 'self'")
                ))
                //No need to activate CORS for now
                .cors(AbstractHttpConfigurer::disable)
                //The use of token-based authentication makes csrf protection irrelevant
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(authorize -> authorize
                        // No auth needed on :
                        .requestMatchers(authorizedPath.getPermitAllPaths()).permitAll()
                        .requestMatchers(HttpMethod.POST, authorizedPath.getPermitsPostPaths()).permitAll()
                        .requestMatchers(HttpMethod.GET, authorizedPath.getPermitsGetPaths()).permitAll()
                        // Others protected routes will be added here.
                        .anyRequest().authenticated()
                )
                // .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(
                        (request, response, exception)
                                -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage())));
        http.addFilterBefore(authenticationJwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
