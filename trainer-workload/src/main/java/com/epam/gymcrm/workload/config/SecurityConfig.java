package com.epam.gymcrm.workload.config;

import com.epam.gymcrm.workload.web.auth.SecurityErrorHandler;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, SecurityErrorHandler securityErrorHandler) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .exceptionHandling(
            exceptionHandling ->
                exceptionHandling
                    .authenticationEntryPoint(securityErrorHandler)
                    .accessDeniedHandler(securityErrorHandler))
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
        .authorizeHttpRequests(
            authorization ->
                authorization
                    .requestMatchers(
                        "/api/v3/api-docs/**",
                        "/api/swagger-ui.html",
                        "/api/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/api/actuator/health",
                        "/api/actuator/info",
                        "/api/actuator/prometheus",
                        "/actuator/health",
                        "/actuator/info",
                        "/actuator/prometheus")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/v1/trainer-workloads/**")
                    .hasRole("SERVICE")
                    .requestMatchers(HttpMethod.POST, "/v1/trainer-workloads")
                    .hasRole("SERVICE")
                    .anyRequest()
                    .authenticated())
        .build();
  }

  /**
   * Validates service JWT tokens signed by Gym CRM system.
   *
   * @param jwtProperties JWT configuration
   * @return JWT decoder
   */
  @Bean
  public JwtDecoder jwtDecoder(JwtProperties jwtProperties) {
    NimbusJwtDecoder jwtDecoder =
        NimbusJwtDecoder.withSecretKey(secretKey(jwtProperties))
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()));
    return jwtDecoder;
  }

  private static JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
    authoritiesConverter.setAuthoritiesClaimName("roles");
    authoritiesConverter.setAuthorityPrefix("ROLE_");

    JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
    authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
    return authenticationConverter;
  }

  private static SecretKey secretKey(JwtProperties jwtProperties) {
    byte[] secret = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
    return new SecretKeySpec(secret, "HmacSHA256");
  }
}
