/*******************************************************************************
 * verinice.veo reporting
 * Copyright (C) 2021  Jochen Kemnade
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.veo.reporting.security;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/** This class bundles custom API security configurations. */
@Configuration
public class ReportingSecurityConfig {

  private static final Logger logger = LoggerFactory.getLogger(ReportingSecurityConfig.class);

  @Value("${veo.reporting.cors.origins}")
  private String[] origins;

  @Value("${veo.reporting.cors.headers}")
  private String[] allowedHeaders;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(
        new Customizer<CsrfConfigurer<HttpSecurity>>() {
          @Override
          @SuppressFBWarnings("SPRING_CSRF_PROTECTION_DISABLED")
          public void customize(CsrfConfigurer<HttpSecurity> csrf) {
            csrf.disable();
          }
        });
    http.cors(Customizer.withDefaults());
    // Anonymous access (a user with role "ROLE_ANONYMOUS" must be
    // enabled for
    // swagger-ui. We cannot disable it.
    // Make sure that no critical API can be accessed by an
    // anonymous user!
    // .anonymous()
    // .disable()
    http.sessionManagement(it -> it.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration corsConfig = new CorsConfiguration();
    corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    // Authorization and Content-Type are always needed, additional headers are configurable:
    corsConfig.addAllowedHeader(HttpHeaders.AUTHORIZATION);
    corsConfig.addAllowedHeader(HttpHeaders.CONTENT_TYPE);

    Arrays.stream(allowedHeaders)
        .forEach(
            s -> {
              logger.debug("Added CORS allowed header: {}", s);
              corsConfig.addAllowedHeader(s);
            });

    Arrays.stream(origins)
        .forEach(
            s -> {
              logger.debug("Added CORS origin pattern: {}", s);
              corsConfig.addAllowedOriginPattern(s);
            });
    corsConfig.setMaxAge(Duration.ofMinutes(30));
    source.registerCorsConfiguration("/**", corsConfig);
    return source;
  }
}
