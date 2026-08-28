Feature: User Login
  As a user of the Workforce Management System
  I want to log in with my credentials
  So that I can access the system

  Background:
    Given the application is running at "http://localhost:3000"

  Scenario: Successful login with valid credentials
    Given I am on the login page
    When I enter email "admin@company.com" and password "password123"
    And I click the login button
    Then I should be redirected to the dashboard
    And I should see the dashboard title

  Scenario: Failed login with invalid credentials
    Given I am on the login page
    When I enter email "invalid@company.com" and password "wrongpassword"
    And I click the login button
    Then I should see an error message "Invalid email or password"
    And I should remain on the login page