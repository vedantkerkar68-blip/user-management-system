Feature: Employee Management
  As an Admin or HR user
  I want to manage employees
  So that I can maintain the workforce records

  Background:
    Given the application is running at "http://localhost:3000"
    And I am logged in as an admin

  Scenario: Admin creates a new employee
    Given I am on the employees page
    When I click "Add Employee"
    And I fill in the employee form with:
      | field         | value          |
      | employeeId    | EMP-001        |
      | fullName      | John Doe       |
      | email         | john@test.com  |
      | password      | password123    |
      | phone         | 1234567890     |
      | department    | Engineering    |
      | designation   | Software Engineer |
      | role          | EMPLOYEE       |
      | joiningDate   | 2024-01-15     |
    And I submit the form
    Then the employee should be created successfully
    And I should see "EMP-001" in the employee list

  Scenario: Admin updates an employee
    Given an employee "EMP-001" exists
    And I am on the employee detail page for "EMP-001"
    When I click "Edit"
    And I update the designation to "Senior Software Engineer"
    And I submit the form
    Then the employee should be updated successfully
    And the designation should show "Senior Software Engineer"

  Scenario: Admin changes employee status
    Given an employee "EMP-001" exists with status "ACTIVE"
    And I am on the employee detail page for "EMP-001"
    When I change the status to "ON_LEAVE"
    Then the employee status should be updated to "ON_LEAVE"

  Scenario: Admin deletes an employee
    Given an employee "EMP-001" exists
    And I am on the employee detail page for "EMP-001"
    When I click "Delete"
    And I confirm the deletion
    Then the employee should be deleted
    And I should not see "EMP-001" in the employee list