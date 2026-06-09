package com.epam.gymcrm.web.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.gymcrm.model.Trainee;
import com.epam.gymcrm.model.Trainer;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class GymUserDetailsServiceTest {

  private static final String USERNAME = "John.Doe";
  private static final String PASSWORD = "encoded-password";

  @Mock private UserRepository userRepository;
  @Mock private TraineeRepository traineeRepository;
  @Mock private TrainerRepository trainerRepository;

  private GymUserDetailsService userDetailsService;

  @BeforeEach
  void setUp() {
    userDetailsService =
        new GymUserDetailsService(userRepository, traineeRepository, trainerRepository);
  }

  @Test
  void loadUserByUsernameShouldReturnActiveTraineeDetails() {
    when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user(true)));
    when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.of(new Trainee()));

    UserDetails result = userDetailsService.loadUserByUsername(USERNAME);

    assertThat(result.getUsername()).isEqualTo(USERNAME);
    assertThat(result.getPassword()).isEqualTo(PASSWORD);
    assertThat(result.isEnabled()).isTrue();
    assertThat(result.getAuthorities())
        .extracting(Object::toString)
        .containsExactly("ROLE_TRAINEE");
    verifyNoInteractions(trainerRepository);
  }

  @Test
  void loadUserByUsernameShouldReturnDisabledTrainerDetails() {
    when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user(false)));
    when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
    when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(new Trainer()));

    UserDetails result = userDetailsService.loadUserByUsername(USERNAME);

    assertThat(result.isEnabled()).isFalse();
    assertThat(result.getAuthorities())
        .extracting(Object::toString)
        .containsExactly("ROLE_TRAINER");
  }

  @Test
  void loadUserByUsernameShouldThrowWhenUserDoesNotExist() {
    when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userDetailsService.loadUserByUsername(USERNAME))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessage("User not found");

    verifyNoInteractions(traineeRepository, trainerRepository);
  }

  @Test
  void loadUserByUsernameShouldThrowWhenUserHasNoProfile() {
    when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user(true)));
    when(traineeRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
    when(trainerRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userDetailsService.loadUserByUsername(USERNAME))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessage("User not found");

    verify(trainerRepository).findByUsername(USERNAME);
  }

  private static User user(boolean active) {
    return User.builder().username(USERNAME).password(PASSWORD).active(active).build();
  }
}
