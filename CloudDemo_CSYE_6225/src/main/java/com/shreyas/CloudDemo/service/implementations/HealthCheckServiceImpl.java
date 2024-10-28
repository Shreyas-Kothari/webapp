package com.shreyas.CloudDemo.service.implementations;

import com.shreyas.CloudDemo.service.interfaces.HealthCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HealthCheckServiceImpl implements HealthCheckService {

    @Qualifier("dbHealthContributor")
    private final HealthContributor healthContributor;

    public HealthCheckServiceImpl(@Qualifier("dbHealthContributor") HealthContributor healthContributor) {
        this.healthContributor = healthContributor;
    }


    public boolean isDataConnectionAvailable() throws Exception {
        try {
            if (healthContributor instanceof HealthIndicator) {
                Health health = ((HealthIndicator) healthContributor).health();
                if (!health.getStatus().equals(Status.UP))
                    log.warn("The database connection is down or service is unavailable");
                else
                    log.info("The database connection is up and service is available");

                return health.getStatus().equals(Status.UP);
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to get health {}", e.getMessage());
            throw e;
        }
    }
}
