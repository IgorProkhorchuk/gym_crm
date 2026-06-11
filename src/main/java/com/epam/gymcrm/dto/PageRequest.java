package com.epam.gymcrm.dto;

public record PageRequest(int page, int size) {

  private static final int DEFAULT_SIZE = 50;
  private static final int MAX_SIZE = 100;

  public PageRequest {
    if (page < 0) {
      throw new IllegalArgumentException("Page cannot be negative");
    }
    if (size <= 0) {
      size = DEFAULT_SIZE;
    }
    if (size > MAX_SIZE) {
      size = MAX_SIZE;
    }
  }

  public static PageRequest firstPage() {
    return new PageRequest(0, DEFAULT_SIZE);
  }
}
