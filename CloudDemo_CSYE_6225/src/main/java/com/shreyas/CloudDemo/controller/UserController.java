package com.shreyas.CloudDemo.controller;

import com.shreyas.CloudDemo.bean.UserBean;
import com.shreyas.CloudDemo.entity.User;
import com.shreyas.CloudDemo.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/users", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController extends BaseController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserBean> createUser(@RequestBody @Valid UserBean user) throws BadRequestException {

        if (userService.isExistingUserByEmail(user.getEmail()))
            throw new BadRequestException("User already exists with this email address.");

        user = userService.createUser(user);
        return CreatedResponse(user);

    }

    @GetMapping("self")
    public ResponseEntity<UserBean> login(Authentication authentication) {
        String emailId = ((User) authentication.getPrincipal()).getEmail();
        UserBean userFound = userService.findByEmail(emailId);
        if (userFound != null) {
            return SuccessResponse(userFound);
        }
        return NoContentFoundResponse("User not found");
    }

    @PutMapping(value = "self")
    public ResponseEntity<UserBean> updateUser(Authentication authentication, @RequestBody @Valid UserBean user) throws BadRequestException {
        String emailId = ((User) authentication.getPrincipal()).getEmail();
        if (!userService.isExistingUserByEmail(emailId))
            throw new BadRequestException("User not found.");

        UserBean updatedUser = userService.updateUser(emailId, user);
        return SuccessResponse(updatedUser);

    }

}
