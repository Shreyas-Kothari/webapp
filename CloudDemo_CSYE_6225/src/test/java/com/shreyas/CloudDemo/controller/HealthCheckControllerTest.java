package com.shreyas.CloudDemo.controller;

import com.shreyas.CloudDemo.config.TestSecurityConfig;
import com.shreyas.CloudDemo.service.interfaces.HealthCheckService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthCheckController.class)
@AutoConfigureMockMvc()
@WithMockUser(username = "user")
@Import(TestSecurityConfig.class)
public class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthCheckService healthCheckService;

    @Test
    public void testHealthCheckSuccess() throws Exception {
        // Mock the service to return true (Status.UP)
        when(healthCheckService.isDataConnectionAvailable()).thenReturn(true);

        mockMvc.perform(get("/healthz"))
                .andExpect(status().isOk()); // Expect 200 OK
    }

    @Test
    public void testHealthCheckServiceUnavailable() throws Exception {
        // Mock the service to return false (Service Unavailable)
        when(healthCheckService.isDataConnectionAvailable()).thenReturn(false);

        mockMvc.perform(get("/healthz"))
                .andExpect(status().isServiceUnavailable()); // Expect 503 Service Unavailable
    }

    @Test
    @Disabled
    public void testGetRequestWithPayloadShouldReturnBadRequest() throws Exception {
        String payload = "{ \"someData\": \"test\" }";

        mockMvc.perform(get("/healthz")
                        .content(payload) // Send a payload in the GET request
                        .contentType(MediaType.ALL))
                .andExpect(status().isBadRequest()); // Expect 400 Bad Request
    }

    @Test
    public void testPostOnGetEndpointShouldReturnMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/healthz")) // Make a POST request instead of GET
                .andExpect(status().isMethodNotAllowed()); // Expect 405 Method Not Allowed
    }
}
