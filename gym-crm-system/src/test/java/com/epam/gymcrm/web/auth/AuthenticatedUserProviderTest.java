package com.epam.gymcrm.web.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.exception.AuthenticationException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class AuthenticatedUserProviderTest {

  private final AuthenticatedUserProvider authenticatedUserProvider =
      new AuthenticatedUserProvider();

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void currentUserShouldReturnTraineePrincipal() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "John.Doe", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TRAINEE"))));

    AuthenticatedPrincipal result = authenticatedUserProvider.currentUser();

    assertThat(result).isEqualTo(new AuthenticatedPrincipal("John.Doe", ProfileType.TRAINEE));
  }

  @Test
  void currentUserShouldReturnTrainerPrincipal() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "Mike.Stone", "n/a", List.of(new SimpleGrantedAuthority("ROLE_TRAINER"))));

    AuthenticatedPrincipal result = authenticatedUserProvider.currentUser();

    assertThat(result).isEqualTo(new AuthenticatedPrincipal("Mike.Stone", ProfileType.TRAINER));
  }

  @Test
  void authenticatedPrincipalToStringShouldExcludeUsername() {
    String representation = new AuthenticatedPrincipal("John.Doe", ProfileType.TRAINEE).toString();

    assertThat(representation)
        .isEqualTo("AuthenticatedPrincipal[profileType=TRAINEE]")
        .doesNotContain("username", "John.Doe");
  }

  @Test
  void currentUserShouldRejectMissingAuthentication() {
    assertThatThrownBy(authenticatedUserProvider::currentUser)
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Authentication is required");
  }

  @Test
  void currentUserShouldRejectUnauthenticatedPrincipal() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("John.Doe", "credentials"));

    assertThatThrownBy(authenticatedUserProvider::currentUser)
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Authentication is required");
  }

  @Test
  void currentUserShouldRejectAnonymousAuthentication() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

    assertThatThrownBy(authenticatedUserProvider::currentUser)
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Authentication is required");
  }

  @Test
  void currentUserShouldRejectPrincipalWithoutSupportedProfileRole() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "Admin.User", "n/a", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

    assertThatThrownBy(authenticatedUserProvider::currentUser)
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Authenticated user profile not found");
  }
}
