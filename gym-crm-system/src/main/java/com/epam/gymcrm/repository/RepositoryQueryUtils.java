package com.epam.gymcrm.repository;

import com.epam.gymcrm.dto.PageRequest;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.data.domain.Pageable;

final class RepositoryQueryUtils {

  private static final LocalDate MIN_DATE = LocalDate.of(1, 1, 1);
  private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

  private RepositoryQueryUtils() {}

  static Pageable toSpringPageRequest(PageRequest pageRequest) {
    PageRequest page = Objects.requireNonNull(pageRequest, "Page request must not be null");
    return org.springframework.data.domain.PageRequest.of(page.page(), page.size());
  }

  static String toExactValue(String value) {
    return isBlank(value) ? "" : value;
  }

  static String toLikePattern(String value) {
    return isBlank(value) ? "" : "%" + value.toLowerCase() + "%";
  }

  static LocalDate fromDate(LocalDate fromDate) {
    return fromDate == null ? MIN_DATE : fromDate;
  }

  static LocalDate toDate(LocalDate toDate) {
    return toDate == null ? MAX_DATE : toDate;
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
