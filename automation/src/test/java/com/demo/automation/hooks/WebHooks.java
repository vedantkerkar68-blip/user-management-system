package com.demo.automation.hooks;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

public class WebHooks {

    private final com.demo.automation.stepdefinitions.CommonSteps commonSteps;

    public WebHooks(com.demo.automation.stepdefinitions.CommonSteps commonSteps) {
        this.commonSteps = commonSteps;
    }

    @Before
    public void setup(Scenario scenario) {
        // Driver is lazily initialized in CommonSteps#getDriver() when first needed.
        // No action required here; ensures hooks are ordered correctly.
    }

    @After
    public void teardown(Scenario scenario) {
        WebDriver driver = null;
        try {
            // Access driver if it was created during scenario
            java.lang.reflect.Field f = com.demo.automation.stepdefinitions.CommonSteps.class.getDeclaredField("driver");
            f.setAccessible(true);
            driver = (WebDriver) f.get(commonSteps);
        } catch (Exception ignored) { }
        if (scenario.isFailed() && driver != null) {
            try {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Failed Scenario Screenshot");
            } catch (Exception ignored) { }
        }
        if (driver != null) {
            try { driver.quit(); } catch (Exception ignored) { }
            try {
                java.lang.reflect.Field f = com.demo.automation.stepdefinitions.CommonSteps.class.getDeclaredField("driver");
                f.setAccessible(true);
                f.set(commonSteps, null);
            } catch (Exception ignored) { }
        }
    }
}