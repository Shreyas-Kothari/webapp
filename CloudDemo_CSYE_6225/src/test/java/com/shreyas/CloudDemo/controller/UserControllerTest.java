package com.shreyas.CloudDemo.controller;


import com.shreyas.CloudDemo.bean.UserBean;
import com.shreyas.CloudDemo.config.TestSecurityConfig;
import com.shreyas.CloudDemo.entity.User;
import com.shreyas.CloudDemo.service.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc()
@Import(TestSecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserBean userBean;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userBean = new UserBean();
        userBean.setFirstName("John");
        userBean.setLastName("Doe");
        userBean.setEmail("john.doe@example.com");
        userBean.setPassword("password123");

        user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("password123");
    }

    @Test
    void createUser_ShouldReturnCreated_WhenUserIsValid() throws Exception {
        when(userService.isExistingUserByEmail(userBean.getEmail())).thenReturn(false);
        when(userService.createUser(any(UserBean.class))).thenReturn(userBean);

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\", \"lastName\":\"Doe\", \"email\":\"john.doe@example.com\", \"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenUserAlreadyExists() throws Exception {
        when(userService.isExistingUserByEmail(userBean.getEmail())).thenReturn(true);

        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\", \"lastName\":\"Doe\", \"email\":\"john.doe@example.com\", \"password\":\"password123\"}"))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser(username = "john.doe@example.com") // Mock authenticated user with username as email
    @Test
    void login_ShouldReturnUserDetails_WhenUserIsAuthenticated() throws Exception {
        // Mock the service call that retrieves the UserBean by email
        when(userService.findByEmail("john.doe@example.com")).thenReturn(userBean);

        // Perform the request and validate the response
        mockMvc.perform(get("/v1/users/self"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @WithMockUser(username = "john.doe@example.com") // Mock authenticated user
    @Test
    void updateUser_ShouldReturnUpdatedUser_WhenUserExists() throws Exception {
        // Mock the service call to check if the user exists
        when(userService.isExistingUserByEmail("john.doe@example.com")).thenReturn(true);

        // Mock the service call to update the user
        when(userService.updateUser(eq("john.doe@example.com"), any(UserBean.class))).thenReturn(userBean);

        // Perform the request and validate the response
        mockMvc.perform(put("/v1/users/self")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"John\", \"lastName\":\"Doe\", \"email\":\"john.doe@example.com\", \"password\":\"newPassword\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }
}
