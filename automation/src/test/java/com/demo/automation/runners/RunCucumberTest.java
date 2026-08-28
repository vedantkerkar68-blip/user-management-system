package com.demo.automation.runners;

import io.cucumber.junit.platform.engine.Constants;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@CucumberContextConfiguration
@SpringBootTest(classes = com.demo.automation.config.CucumberTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("local")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.demo.automation.stepdefinitions")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-report.html, json:target/cucumber-report.json")
public class RunCucumberTest {
}