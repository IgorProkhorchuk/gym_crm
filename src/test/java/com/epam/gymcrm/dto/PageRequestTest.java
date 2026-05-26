package com.epam.gymcrm.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;

class PageRequestTest {

  @Test
  void firstPageShouldUseDefaultLimit() {
    PageRequest pageRequest = PageRequest.firstPage();

    assertAll(
        () -> assertThat(pageRequest.offset()).isZero(),
        () -> assertThat(pageRequest.limit()).isEqualTo(50));
  }

  @Test
  void constructorShouldRejectNegativeOffset() {
    assertThatThrownBy(() -> new PageRequest(-1, 10))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Offset cannot be negative");
  }

  @Test
  void constructorShouldUseDefaultLimitWhenLimitIsNotPositive() {
    assertAll(
        () -> assertThat(new PageRequest(0, 0).limit()).isEqualTo(50),
        () -> assertThat(new PageRequest(0, -5).limit()).isEqualTo(50));
  }

  @Test
  void constructorShouldClampLimitToMaximum() {
    assertThat(new PageRequest(0, 500).limit()).isEqualTo(100);
  }
}
