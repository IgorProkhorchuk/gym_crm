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

  @positive
  Scenario: Register a trainee profile
    When the client creates trainee "Bdd" "Trainee" born on "1995-01-10" with address "Main Street, 123"
    Then the response status should be 201
    And the response should contain username "Bdd.Trainee"
    And the response should contain a generated password
