@component @workload @messaging
Feature: Trainer workload message handling

  @positive
  Scenario: Add trainer workload from a valid message
    When the workload listener receives an "ADD" event for trainer "Training.Trainer" with duration 60
    Then the workload service should receive an "ADD" request for trainer "Training.Trainer"
    And the workload request should contain duration 60 minutes for date "2026-05-03"

  @negative
  Scenario: Reject invalid trainer workload message
    When the workload listener receives an invalid "ADD" event for trainer "" with duration 0
    Then the workload message should be rejected with error "ConstraintViolationException"
    And the workload service should not receive a workload request
