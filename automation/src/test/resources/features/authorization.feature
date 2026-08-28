Feature: Authorization and Role-Based Access Control
  As a user of the system
  I want to have appropriate access based on my role
  So that I can only perform actions I'm authorized for

  Background:
    Given the application is running at "http://localhost:3000"

  Scenario: Admin can access all pages
    Given I am logged in as an admin
    When I navigate to the dashboard
    Then I should see the dashboard
    When I navigate to the employees page
    Then I should see the employees list
    When I navigate to the audit logs page
    Then I should see the audit logs

  Scenario: HR can access employees but not audit logs
    Given I am logged in as HR
    When I navigate to the employees page
    Then I should see the employees list
    When I navigate to the audit logs page
    Then I should be redirected or see access denied

  Scenario: Manager can view employees but not modify
    Given I am logged in as a manager
    When I navigate to the employees page
    Then I should see the employees list
    When I try to access the add employee page
    Then I should be redirected or see access denied

  Scenario: Employee can only view own profile
    Given I am logged in as an employee
    When I navigate to the dashboard
    Then I should see the dashboard
    When I try to access the employees page
    Then I should be redirected or see access denied