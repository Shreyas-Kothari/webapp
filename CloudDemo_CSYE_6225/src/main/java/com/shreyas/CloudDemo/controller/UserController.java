package com.shreyas.CloudDemo.controller;

import com.shreyas.CloudDemo.bean.UserBean;
import com.shreyas.CloudDemo.bean.UserProfilePicBean;
import com.shreyas.CloudDemo.service.interfaces.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/users", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController extends BaseController {
    private final UserService userService;

    @RequestMapping(path = "", method = {RequestMethod.HEAD, RequestMethod.OPTIONS})
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public void unSupportedMethods() {
    }

    @RequestMapping(path = "/self", method = {RequestMethod.HEAD, RequestMethod.OPTIONS})
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public void unSupportedMethodsForSelf() {
    }


    @PreAuthorize("permitAll()")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserBean> createUser(HttpServletRequest request, @RequestBody @Valid UserBean user) throws BadRequestException {
        if (!request.getParameterMap().isEmpty())
            throw new BadRequestException("Request Param not allowed");

        if (user.getEmail() == null)
            throw new BadRequestException("Email cannot be empty.");

        user = userService.createUser(user);
        return CreatedResponse(user);
    }

    @GetMapping("/self")
    public ResponseEntity<UserBean> getUserDetails(HttpServletRequest request, Authentication authentication) throws BadRequestException {
        if (request.getContentLength() > 0 || !request.getParameterMap().isEmpty())
            throw new BadRequestException("Request Body/Param not allowed");

        String emailId = ((UserDetails) authentication.getPrincipal()).getUsername();
        UserBean userFound = userService.findByEmail(emailId);
        if (userFound != null) {
            return SuccessResponse(userFound);
        }
        return NoContentResponse();
    }

    @PutMapping(value = "/self", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserBean> updateUser(HttpServletRequest request, Authentication authentication, @RequestBody @Valid UserBean user) throws BadRequestException {
        if (!request.getParameterMap().isEmpty())
            throw new BadRequestException("Request param not allowed");

        String emailId = ((UserDetails) authentication.getPrincipal()).getUsername();

        if (user.getEmail() != null)
            throw new BadRequestException("Email cannot be updated.");

        if (!userService.isExistingUserByEmail(emailId))
            throw new BadRequestException("User not found.");

        userService.updateUser(emailId, user);
        return NoContentResponse();

    }

    @GetMapping(value = "/self/pic", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserProfilePicBean> getUserProfilePicture(HttpServletRequest request, Authentication authentication) throws BadRequestException {
        if (request.getContentLength() > 0 || !request.getParameterMap().isEmpty())
            throw new BadRequestException("Request Body/Param not allowed");

        String emailId = ((UserDetails) authentication.getPrincipal()).getUsername();

        UserProfilePicBean profilePicBean = userService.getUserProfilePicture(emailId);
        if (profilePicBean == null)
            return ErrorResponse(HttpStatus.NOT_FOUND, null);
        return SuccessResponse(profilePicBean);
    }

    @PostMapping(value = "/self/pic")
    public ResponseEntity<UserProfilePicBean> uploadUserProfilePicture(HttpServletRequest request, Authentication authentication, @RequestParam("profilePic") MultipartFile file) throws BadRequestException {
        if (!request.getParameterMap().isEmpty())
            throw new BadRequestException("Request param not allowed");

        String emailId = ((UserDetails) authentication.getPrincipal()).getUsername();

        UserProfilePicBean profilePicBean = userService.uploadUserProfilePicture(emailId, file);
        if(profilePicBean==null)
            return ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,null);
        return SuccessResponse(profilePicBean);
    }

    @DeleteMapping("/self/pic")
    public ResponseEntity<Void> deleteUser(HttpServletRequest request, Authentication authentication) throws BadRequestException {
        if (!request.getParameterMap().isEmpty())
            throw new BadRequestException("Request param not allowed");

        String emailId = ((UserDetails) authentication.getPrincipal()).getUsername();

        boolean isDeleted = userService.deleteUserProfilePicture(emailId);
        if (isDeleted)
            return NoContentResponse();
        else
            return ErrorResponse(HttpStatus.NOT_FOUND);
    }

}
