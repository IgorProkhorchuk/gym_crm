package com.epam.gymcrm.web.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomTokenServiceTest {

  private TokenService tokenService;

  @BeforeEach
  void setUp() {
    tokenService = new CustomTokenService();
  }

  @Test
  void createTokenShouldStoreAuthenticatedUser() {
    AuthenticatedUser user = new AuthenticatedUser("John.Doe", "password", ProfileType.TRAINEE);

    String token = tokenService.createToken(user);

    assertThat(token).isNotBlank();
    assertThat(tokenService.getUserByToken(token)).isEqualTo(user);
  }

  @Test
  void createTokenShouldGenerateDifferentTokensForDifferentCalls() {
    AuthenticatedUser user = new AuthenticatedUser("John.Doe", "password", ProfileType.TRAINEE);

    String firstToken = tokenService.createToken(user);
    String secondToken = tokenService.createToken(user);

    assertThat(firstToken).isNotEqualTo(secondToken);
  }

  @Test
  void createTokenShouldThrowWhenUserIsNull() {
    assertThatThrownBy(() -> tokenService.createToken(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Authenticated user must not be null");
  }

  @Test
  void getUserByTokenShouldThrowWhenTokenIsBlank() {
    assertThatThrownBy(() -> tokenService.getUserByToken(" "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Authentication token must not be blank");
  }

  @Test
  void getUserByTokenShouldThrowWhenTokenIsUnknown() {
    assertThatThrownBy(() -> tokenService.getUserByToken("unknown-token"))
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("Invalid authentication token");
  }

  @Test
  void updatePasswordShouldReplacePasswordForExistingToken() {
    AuthenticatedUser user = new AuthenticatedUser("John.Doe", "old-password", ProfileType.TRAINEE);
    String token = tokenService.createToken(user);

    tokenService.updatePassword(token, "new-password");

    assertThat(tokenService.getUserByToken(token))
        .isEqualTo(new AuthenticatedUser("John.Doe", "new-password", ProfileType.TRAINEE));
  }

  @Test
  void updatePasswordShouldThrowWhenNewPasswordIsBlank() {
    AuthenticatedUser user = new AuthenticatedUser("John.Doe", "old-password", ProfileType.TRAINEE);
    String token = tokenService.createToken(user);

    assertThatThrownBy(() -> tokenService.updatePassword(token, " "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("New password must not be blank");
  }
}
