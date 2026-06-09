package com.epam.gymcrm.web.auth;

import com.epam.gymcrm.dto.auth.ProfileType;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GymUserDetailsService implements UserDetailsService {

  private static final String USER_NOT_FOUND = "User not found";

  private final UserRepository userRepository;
  private final TraineeRepository traineeRepository;
  private final TrainerRepository trainerRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) {
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND));
    ProfileType profileType = resolveProfileType(username);

    return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
        .password(user.getPassword())
        .authorities("ROLE_" + profileType.name())
        .disabled(!Boolean.TRUE.equals(user.getActive()))
        .build();
  }

  private ProfileType resolveProfileType(String username) {
    if (traineeRepository.findByUsername(username).isPresent()) {
      return ProfileType.TRAINEE;
    }
    if (trainerRepository.findByUsername(username).isPresent()) {
      return ProfileType.TRAINER;
    }
    throw new UsernameNotFoundException(USER_NOT_FOUND);
  }
}
