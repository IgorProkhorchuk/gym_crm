package com.epam.gymcrm.web.auth;

import com.epam.gymcrm.exception.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonBlank;
import static com.epam.gymcrm.service.validation.ServiceValidationUtils.requireNonNull;

@Service
public class FakeTokenService {

  private final Map<String, AuthenticatedUser> authenticatedUsers = new ConcurrentHashMap<>();

  public String createToken(AuthenticatedUser user) {
    requireNonNull(user, "Authenticated user must not be null");
    String token = UUID.randomUUID().toString();
    authenticatedUsers.put(token, user);
    return token;
  }

  public AuthenticatedUser getUserByToken(String token) {
    requireNonBlank(token, "Authentication token must not be blank");
    AuthenticatedUser user = authenticatedUsers.get(token);
    if (user == null) {
      throw new AuthenticationException("Invalid authentication token");
    }
    return user;
  }
}

