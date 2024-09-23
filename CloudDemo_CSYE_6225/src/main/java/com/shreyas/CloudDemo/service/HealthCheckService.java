package com.shreyas.CloudDemo.service;

import java.sql.SQLException;

public interface HealthCheckService {
    boolean isDataConnectionAvailable() throws Exception;
}
