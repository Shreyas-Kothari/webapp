package com.shreyas.CloudDemo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthIndicator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {

    @Mock
    private HealthContributor healthContributor;

    @InjectMocks
    private HealthCheckService healthCheckService;

    @Test
    void whenHealthContributorIsUp_thenReturnTrue() throws Exception {
        HealthIndicator healthIndicator = (HealthIndicator) healthContributor;
        when(healthIndicator.health()).thenReturn(Health.up().build());

        assertTrue(healthCheckService.isDataConnectionAvailable());
    }

    @Test
    void whenHealthContributorIsDown_thenReturnFalse() throws Exception {
        HealthIndicator healthIndicator = (HealthIndicator) healthContributor;
        when(healthIndicator.health()).thenReturn(Health.down().build());

        assertFalse(healthCheckService.isDataConnectionAvailable());
    }

    @Test
    void whenHealthContributorThrowsException_thenThrowException() {
        when(((HealthIndicator) healthContributor).health()).thenThrow(RuntimeException.class);

        assertThrows(Exception.class, () -> healthCheckService.isDataConnectionAvailable());
    }
}
