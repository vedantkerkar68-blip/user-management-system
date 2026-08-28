Feature: Validation and Error Handling
  As a user of the system
  I want to receive clear validation messages
  So that I can correct my input

  Background:
    Given the application is running at "http://localhost:3000"
    And I am logged in as an admin

  Scenario: Creating employee with invalid email
    Given I am on the add employee page
    When I fill in the form with invalid email "invalid-email"
    And I submit the form
    Then I should see a validation error for email

  Scenario: Creating employee with duplicate email
    Given an employee with email "john@test.com" exists
    And I am on the add employee page
    When I fill in the form with email "john@test.com"
    And I submit the form
    Then I should see a validation error for duplicate email

  Scenario: Creating employee with missing required fields
    Given I am on the add employee page
    When I submit the form without filling required fields
    Then I should see validation errors for all required fields

  Scenario: Creating employee with short password
    Given I am on the add employee page
    When I fill in the form with password "123"
    And I submit the form
    Then I should see a validation error for password length