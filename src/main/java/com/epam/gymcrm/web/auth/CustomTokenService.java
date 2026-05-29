package com.epam.gymcrm.web.auth;

import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonBlank;
import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonNull;

import com.epam.gymcrm.exception.AuthenticationException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class CustomTokenService implements TokenService {

  private final Map<String, AuthenticatedUser> authenticatedUsers = new ConcurrentHashMap<>();

  @Override
  public String createToken(AuthenticatedUser user) {
    requireNonNull(user, "Authenticated user must not be null");
    String token = UUID.randomUUID().toString();
    authenticatedUsers.put(token, user);
    return token;
  }

  @Override
  public AuthenticatedUser getUserByToken(String token) {
    requireNonBlank(token, "Authentication token must not be blank");
    AuthenticatedUser user = authenticatedUsers.get(token);
    if (user == null) {
      throw new AuthenticationException("Invalid authentication token");
    }
    return user;
  }

  @Override
  public void updatePassword(String token, String newPassword) {
    requireNonBlank(newPassword, "New password must not be blank");
    AuthenticatedUser user = getUserByToken(token);
    authenticatedUsers.put(
        token,
        new AuthenticatedUser(
            user.username(), newPassword, user.profileType(), user.userId(), user.profileId()));
  }
}
