@component @workload @security
Feature: Trainer workload endpoint security

  @negative
  Scenario Outline: Reject unauthorized workload requests
    When the client requests trainer workload for "<username>" using "<credential>"
    Then the response status should be <status>

    Examples:
      | username   | credential | status |
      | Mike.Stone | no JWT     | 401    |
      | Mike.Stone | USER JWT   | 403    |

  @negative
  Scenario: Return not found for authorized request to missing trainer workload
    When the service client requests missing trainer workload for "Missing.Trainer" with a service JWT
    Then the response status should be 404

  @positive
  Scenario: Return trainer workload for authorized service request
    When the service client requests existing trainer workload for "Mike.Stone" with a service JWT
    Then the response status should be 200
    And the response should contain trainer username "Mike.Stone"
    And the response should contain workload duration 120 minutes for year 2026 and month 7
