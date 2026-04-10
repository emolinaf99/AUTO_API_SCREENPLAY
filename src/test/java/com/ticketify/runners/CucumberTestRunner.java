package com.ticketify.runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.ticketify.stepdefinitions", "com.ticketify.hooks"},
        plugin = {"pretty"}
)
public class CucumberTestRunner {
}
