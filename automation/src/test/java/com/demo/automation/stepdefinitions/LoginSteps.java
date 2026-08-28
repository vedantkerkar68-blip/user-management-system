package com.demo.automation.stepdefinitions;

import io.cucumber.java.en.*;

public class LoginSteps {

    private final CommonSteps common;

    public LoginSteps(CommonSteps common) {
        this.common = common;
    }

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        common.navigateToLoginPage();
    }

    @When("I enter email {string} and password {string}")
    public void iEnterEmailAndPassword(String email, String password) {
        common.enterEmailAndPassword(email, password);
    }

    @When("I click the login button")
    public void iClickTheLoginButton() {
        common.clickLoginButton();
    }

    @Then("I should be redirected to the dashboard")
    public void iShouldBeRedirectedToTheDashboard() {
        common.waitForUrl("/");
    }

    @Then("I should see an error message {string}")
    public void iShouldSeeAnErrorMessage(String message) {
        common.waitForElement(org.openqa.selenium.By.xpath("//*[contains(text(),'" + message + "')]"));
    }

    @Then("I should remain on the login page")
    public void iShouldRemainOnTheLoginPage() {
        String currentUrl = common.getDriver().getCurrentUrl();
        assert currentUrl.contains("/login") : "Expected to remain on login page";
    }
}