@integration @workload @positive
Feature: Trainer workload integration

  Scenario: Start trainer workload integration BDD context
    Given the trainer workload integration context is ready
    Then the trainer workload REST API should be available in the test context
