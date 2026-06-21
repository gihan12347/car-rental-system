package com.carrental.config;

import com.carrental.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final DatabaseUserDetailsService userDetailsService;
    private final FleetDeskAuthenticationSuccessHandler fleetDeskAuthenticationSuccessHandler;
    private final FleetDeskAuthenticationFailureHandler fleetDeskAuthenticationFailureHandler;
    private final FriendlyAccessDeniedHandler friendlyAccessDeniedHandler;


    public SecurityConfig(DatabaseUserDetailsService userDetailsService,
                          FleetDeskAuthenticationSuccessHandler fleetDeskAuthenticationSuccessHandler, FleetDeskAuthenticationFailureHandler fleetDeskAuthenticationFailureHandler, FriendlyAccessDeniedHandler friendlyAccessDeniedHandler) {
        this.userDetailsService = userDetailsService;
        this.fleetDeskAuthenticationSuccessHandler = fleetDeskAuthenticationSuccessHandler;
        this.fleetDeskAuthenticationFailureHandler = fleetDeskAuthenticationFailureHandler;
        this.friendlyAccessDeniedHandler = friendlyAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(auth -> auth
                        .antMatchers(
                                "/login",
                                "/api/auth/status",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/cars/**",
                                "/uploads/employees/**",
                                "/uploads/profiles/**",
                                "/sw.js",
                                "/site.webmanifest",
                                "/favicon.ico",
                                "/icons/**",
                                "/media/**",
                                "/error"
                        ).permitAll()
                        .antMatchers(HttpMethod.POST, "/account/users").hasRole("ADMIN")
                        .antMatchers(HttpMethod.GET, "/employees").hasRole("ADMIN")
                        .antMatchers(HttpMethod.GET, "/office-expenses").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .invalidSessionUrl("/login?expired=1"))
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(friendlyAccessDeniedHandler))
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(fleetDeskAuthenticationSuccessHandler)
                        .failureHandler(fleetDeskAuthenticationFailureHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?signedOut=1")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .headers(headers -> {
                    headers.cacheControl();
                    headers.referrerPolicy(
                            ReferrerPolicyHeaderWriter.ReferrerPolicy
                                    .STRICT_ORIGIN_WHEN_CROSS_ORIGIN
                    );
                })
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
