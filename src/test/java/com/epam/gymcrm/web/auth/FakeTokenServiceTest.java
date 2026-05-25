package com.epam.gymcrm.web.auth;

import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FakeTokenServiceTest {

    private FakeTokenService fakeTokenService;

    @BeforeEach
    void setUp() {
        fakeTokenService = new FakeTokenService();
    }

    @Test
    void createTokenShouldStoreAuthenticatedUser() {
        AuthenticatedUser user = new AuthenticatedUser("John.Doe", "password", ProfileType.TRAINEE);

        String token = fakeTokenService.createToken(user);

        assertThat(token).isNotBlank();
        assertThat(fakeTokenService.getUserByToken(token)).isEqualTo(user);
    }

    @Test
    void createTokenShouldGenerateDifferentTokensForDifferentCalls() {
        AuthenticatedUser user = new AuthenticatedUser("John.Doe", "password", ProfileType.TRAINEE);

        String firstToken = fakeTokenService.createToken(user);
        String secondToken = fakeTokenService.createToken(user);

        assertThat(firstToken).isNotEqualTo(secondToken);
    }

    @Test
    void createTokenShouldThrowWhenUserIsNull() {
        assertThatThrownBy(() -> fakeTokenService.createToken(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Authenticated user must not be null");
    }

    @Test
    void getUserByTokenShouldThrowWhenTokenIsBlank() {
        assertThatThrownBy(() -> fakeTokenService.getUserByToken(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Authentication token must not be blank");
    }

    @Test
    void getUserByTokenShouldThrowWhenTokenIsUnknown() {
        assertThatThrownBy(() -> fakeTokenService.getUserByToken("unknown-token"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid authentication token");
    }

    @Test
    void updatePasswordShouldReplacePasswordForExistingToken() {
        AuthenticatedUser user = new AuthenticatedUser("John.Doe", "old-password", ProfileType.TRAINEE);
        String token = fakeTokenService.createToken(user);

        fakeTokenService.updatePassword(token, "new-password");

        assertThat(fakeTokenService.getUserByToken(token))
                .isEqualTo(new AuthenticatedUser("John.Doe", "new-password", ProfileType.TRAINEE));
    }

    @Test
    void updatePasswordShouldThrowWhenNewPasswordIsBlank() {
        AuthenticatedUser user = new AuthenticatedUser("John.Doe", "old-password", ProfileType.TRAINEE);
        String token = fakeTokenService.createToken(user);

        assertThatThrownBy(() -> fakeTokenService.updatePassword(token, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New password must not be blank");
    }
}
