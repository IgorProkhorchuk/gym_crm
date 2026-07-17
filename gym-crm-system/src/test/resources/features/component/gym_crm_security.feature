@component @gym-crm @security
Feature: Gym CRM endpoint security

  @negative
  Scenario: Reject trainee profile request without JWT
    When the client requests trainee profile for "John.Doe" without a JWT
    Then the response status should be 401
