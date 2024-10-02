package com.shreyas.CloudDemo.controller;

import com.shreyas.CloudDemo.bean.UserBean;
import com.shreyas.CloudDemo.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/users", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController extends BaseController {
    private final UserService userService;

    @PreAuthorize("permitAll()")
    @PostMapping
    public ResponseEntity<UserBean> createUser(@RequestBody @Valid UserBean user) throws BadRequestException {

        if(user.getEmail()==null)
            throw new BadRequestException("Email cannot be empty.");

        user = userService.createUser(user);
        return CreatedResponse(user);

    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("self")
    public ResponseEntity<UserBean> login(Authentication authentication, @RequestBody(required = false) Object user) throws BadRequestException{

        if(user!=null)
            throw new BadRequestException("Request body is not required.");

        String emailId = ((UserDetails) authentication.getPrincipal()).getUsername();
        UserBean userFound = userService.findByEmail(emailId);
        if (userFound != null) {
            return SuccessResponse(userFound);
        }
        return NoContentResponse();
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "self")
    public ResponseEntity<UserBean> updateUser(Authentication authentication, @RequestBody @Valid UserBean user) throws BadRequestException {
        String emailId = ((UserDetails) authentication.getPrincipal()).getUsername();

        if(user.getEmail() != null)
            throw new BadRequestException("Email cannot be updated.");

        if (!userService.isExistingUserByEmail(emailId))
            throw new BadRequestException("User not found.");

        userService.updateUser(emailId, user);
        return NoContentResponse();

    }

}
