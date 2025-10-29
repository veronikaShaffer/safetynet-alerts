package com.example.safetynetalerts;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

@SpringBootTest
class SafetynetAlertsApplicationTests {

    @Test
    void main_runsSpringApplication() {
        // Arrange: mock SpringApplication.run
        try (var mocked = mockStatic(SpringApplication.class)) {
            // Act
            SafetynetAlertsApplication.main(new String[]{"test"});

            // Assert
            mocked.verify(() ->
                    SpringApplication.run(SafetynetAlertsApplication.class, new String[]{"test"}), times(1));
        }
    }

}
