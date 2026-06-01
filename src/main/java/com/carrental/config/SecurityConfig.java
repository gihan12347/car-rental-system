package com.carrental.config;

import com.carrental.security.DatabaseUserDetailsService;
import com.carrental.security.FleetDeskAuthenticationSuccessHandler;
import com.carrental.security.FleetDeskLogoutSuccessHandler;
import com.carrental.security.FriendlyAccessDeniedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final DatabaseUserDetailsService userDetailsService;

    public SecurityConfig(DatabaseUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(auth -> auth
                        .antMatchers(
                                "/login",
                                "/api/auth/status",
                                "/css/**",
                                "/images/**",
                                "/uploads/cars/**",
                                "/sw.js",
                                "/site.webmanifest",
                                "/favicon.ico",
                                "/icons/**",
                                "/media/**",
                                "/error"
                        ).permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .invalidSessionUrl("/login?expired=1"))
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(new FriendlyAccessDeniedHandler()))
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(new FleetDeskAuthenticationSuccessHandler("/dashboard"))
                        .failureUrl("/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(new FleetDeskLogoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .headers(headers -> {
                    headers.cacheControl();
                    headers.referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN);
                })
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
