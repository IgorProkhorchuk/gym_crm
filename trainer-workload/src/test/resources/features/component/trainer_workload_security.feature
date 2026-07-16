@component @workload @security @negative
Feature: Trainer workload endpoint security

  Scenario: Reject workload request without JWT
    When the client requests trainer workload without a JWT
    Then the response status should be 401
