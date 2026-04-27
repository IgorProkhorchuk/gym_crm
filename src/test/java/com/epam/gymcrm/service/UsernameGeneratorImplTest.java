package com.epam.gymcrm.service;

import com.epam.gymcrm.service.impl.UsernameGeneratorImpl;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UsernameGeneratorImplTest {

    private final UsernameGenerator usernameGenerator = new UsernameGeneratorImpl();

    @Test
    void testGenerateReturnsBaseUsernameWhenAvailable() {
        String username = usernameGenerator.generate("John", "Doe", Set.of());

        assertThat(username).isEqualTo("John.Doe");
    }

    @Test
    void testGenerateAddsSuffixWhenBaseUsernameExists() {
        String username = usernameGenerator.generate("John", "Doe", Set.of("John.Doe"));

        assertThat(username).isEqualTo("John.Doe1");
    }

    @Test
    void testGenerateSkipsTakenSequentialSuffixes() {
        String username = usernameGenerator.generate(
                "John",
                "Doe",
                Set.of("John.Doe", "John.Doe1")
        );

        assertThat(username).isEqualTo("John.Doe2");
    }

    @Test
    void testGenerateFillsFirstAvailableSuffixGap() {
        String username = usernameGenerator.generate(
                "John",
                "Doe",
                Set.of("John.Doe", "John.Doe2", "John.Doering")
        );

        assertThat(username).isEqualTo("John.Doe1");
    }
}
