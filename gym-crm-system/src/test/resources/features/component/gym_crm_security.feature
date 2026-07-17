@component @gym-crm @security
Feature: Gym CRM endpoint security

  @negative
  Scenario Outline: Reject unauthorized trainee profile requests
    When the client requests trainee profile for "<username>" using "<credential>"
    Then the response status should be <status>

    Examples:
      | username | credential  | status |
      | John.Doe | no JWT      | 401    |
      | John.Doe | TRAINER JWT | 403    |
