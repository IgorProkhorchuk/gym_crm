package com.epam.gymcrm.web.auth;

import com.epam.gymcrm.dto.auth.ProfileType;

public record AuthenticatedPrincipal(String username, ProfileType profileType) {}
