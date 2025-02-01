package com.example.bookapi.config;

import com.example.bookapi.OnedemoApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@CucumberContextConfiguration
@SpringBootTest(
    classes = OnedemoApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "management.endpoints.web.base-path=/actuator",
        "management.endpoints.web.exposure.include=health",
        "management.endpoint.health.enabled=true"
    }
)
public class CucumberTestConfig {

    @LocalServerPort
    private int port;

} 