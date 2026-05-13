package passroutebackend.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import passroutebackend.global.property.CorsProperties;
import passroutebackend.global.property.FollowUpProperties;
import passroutebackend.global.property.SwaggerProperties;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties({CorsProperties.class, SwaggerProperties.class, FollowUpProperties.class})
public class SecurityConfig {

  private static final String[] PUBLIC_POST = {
    "/auth/login",
    "/auth/signup",
    "/auth/reissue"
  };

  private static final String[] PUBLIC_GET = {
    "/",
    "/error",

    // Swagger
    "/swagger-ui.html",
    "/swagger-ui/**",
    "/v3/api-docs",
    "/v3/api-docs/**",

    // Health Check
    "/actuator/health"
  };

  private final CorsProperties corsProperties;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(AbstractHttpConfigurer::disable)
      .formLogin(AbstractHttpConfigurer::disable)
      .httpBasic(AbstractHttpConfigurer::disable)

      .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )

      .cors(cors -> cors.configurationSource(corsConfigurationSource()))

      .authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
        .requestMatchers(HttpMethod.POST, PUBLIC_POST).permitAll()
        .requestMatchers(HttpMethod.GET, PUBLIC_GET).permitAll()
        .anyRequest().authenticated()
      );

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    return request -> {
      CorsConfiguration configuration = new CorsConfiguration();

      configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
      configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
      configuration.setAllowedHeaders(List.of("*"));
      configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
      configuration.setAllowCredentials(true);
      configuration.setMaxAge(3600L);

      return configuration;
    };
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}