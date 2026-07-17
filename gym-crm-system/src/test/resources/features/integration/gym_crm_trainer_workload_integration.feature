@integration @gym-crm @workload @positive
Feature: Gym CRM trainer workload integration

  Scenario: Create trainer workload outbox event after training is added
    Given an active trainee "Integration.Trainee" and trainer "Integration.Trainer" exist
    When the trainee "Integration.Trainee" adds "Yoga Basics" training with trainer "Integration.Trainer" on "2026-05-03" for 60 minutes
    Then the training response status should be 200
    And gym crm should create a pending trainer workload outbox event
    And the outbox event should contain trainer "Integration.Trainer", date "2026-05-03", duration 60 and action "ADD"
