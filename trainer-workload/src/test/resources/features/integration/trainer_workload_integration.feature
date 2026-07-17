@integration @workload @messaging @positive
Feature: Trainer workload integration

  Scenario Outline: Update trainer workload after receiving workload message
    When the workload listener receives an "<actionType>" integration event with training id <trainingId> for trainer "<trainerUsername>" on "<trainingDate>" with duration <duration>
    Then the trainer workload for "<trainerUsername>" should contain <duration> minutes for year <year> and month <month>
    And the processed workload event with training id <trainingId> and action "<actionType>" should be recorded

    Examples:
      | actionType | trainingId | trainerUsername     | trainingDate | duration | year | month |
      | ADD        | 9201       | Integration.Trainer | 2026-05-03   | 60       | 2026 | 5     |
