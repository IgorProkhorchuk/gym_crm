package com.epam.gymcrm.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;

class PageRequestTest {

  @Test
  void firstPageShouldUseDefaultSize() {
    PageRequest pageRequest = PageRequest.firstPage();

    assertAll(
        () -> assertThat(pageRequest.page()).isZero(),
        () -> assertThat(pageRequest.size()).isEqualTo(50));
  }

  @Test
  void constructorShouldRejectNegativePage() {
    assertThatThrownBy(() -> new PageRequest(-1, 10))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Page cannot be negative");
  }

  @Test
  void constructorShouldUseDefaultSizeWhenSizeIsNotPositive() {
    assertAll(
        () -> assertThat(new PageRequest(0, 0).size()).isEqualTo(50),
        () -> assertThat(new PageRequest(0, -5).size()).isEqualTo(50));
  }

  @Test
  void constructorShouldClampSizeToMaximum() {
    assertThat(new PageRequest(0, 500).size()).isEqualTo(100);
  }
}
