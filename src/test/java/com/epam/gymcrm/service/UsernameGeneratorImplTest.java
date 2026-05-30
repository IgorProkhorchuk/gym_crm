package com.epam.gymcrm.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.gymcrm.service.impl.UsernameGeneratorImpl;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UsernameGeneratorImplTest {

  private final UsernameGenerator usernameGenerator = new UsernameGeneratorImpl();

  @Test
  void generateShouldReturnBaseUsernameWhenItIsAvailable() {
    String username = usernameGenerator.generate("John", "Doe", Set.of());

    assertThat(username).isEqualTo("John.Doe");
  }

  @Test
  void generateShouldAddSuffixWhenBaseUsernameExists() {
    String username = usernameGenerator.generate("John", "Doe", Set.of("John.Doe"));

    assertThat(username).isEqualTo("John.Doe1");
  }

  @Test
  void generateShouldSkipTakenSequentialSuffixes() {
    String username = usernameGenerator.generate("John", "Doe", Set.of("John.Doe", "John.Doe1"));

    assertThat(username).isEqualTo("John.Doe2");
  }

  @Test
  void generateShouldReturnFirstAvailableSuffixWhenGapExists() {
    String username =
        usernameGenerator.generate("John", "Doe", Set.of("John.Doe", "John.Doe2", "John.Doering"));

    assertThat(username).isEqualTo("John.Doe1");
  }
}
