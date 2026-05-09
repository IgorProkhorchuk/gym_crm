package com.epam.gymcrm.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class TrainingMapperTest {

    private final TrainingMapper trainingMapper = Mappers.getMapper(TrainingMapper.class);

    @Test
    void fullNameShouldJoinFirstAndLastName() {
        assertThat(trainingMapper.fullName("Jane", "Doe")).isEqualTo("Jane Doe");
    }
}
