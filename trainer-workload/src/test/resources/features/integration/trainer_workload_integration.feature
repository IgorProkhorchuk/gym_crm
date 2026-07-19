@integration @workload @messaging
Feature: Trainer workload integration

  @positive
  Scenario Outline: Update trainer workload after receiving workload message
    When the workload listener receives an "<actionType>" integration event with training id <trainingId> for trainer "<trainerUsername>" on "<trainingDate>" with duration <duration>
    Then the trainer workload for "<trainerUsername>" should contain <duration> minutes for year <year> and month <month>
    And the processed workload event with training id <trainingId> and action "<actionType>" should be recorded

    Examples:
      | actionType | trainingId | trainerUsername     | trainingDate | duration | year | month |
      | ADD        | 9201       | Integration.Trainer | 2026-05-03   | 60       | 2026 | 5     |

  @negative
  Scenario: Reject invalid workload message without changing Mongo state
    When the workload listener receives an invalid "ADD" integration event with training id 9202 for trainer "" on "2026-05-03" with duration 0
    Then the integration message should be rejected with error "ConstraintViolationException"
    And no trainer workload should exist for ""
    And the processed workload event with training id 9202 and action "ADD" should not be recorded
