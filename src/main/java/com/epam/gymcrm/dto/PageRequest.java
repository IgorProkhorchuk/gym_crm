package com.epam.gymcrm.dto;

public record PageRequest(int offset, int limit) {

  private static final int DEFAULT_LIMIT = 50;
  private static final int MAX_LIMIT = 100;

  public PageRequest {
    if (offset < 0) {
      throw new IllegalArgumentException("Offset cannot be negative");
    }
    if (limit <= 0) {
      limit = DEFAULT_LIMIT;
    }
    if (limit > MAX_LIMIT) {
      limit = MAX_LIMIT;
    }
  }

  public static PageRequest firstPage() {
    return new PageRequest(0, DEFAULT_LIMIT);
  }
}
