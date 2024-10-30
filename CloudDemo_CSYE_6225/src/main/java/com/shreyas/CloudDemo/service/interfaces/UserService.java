package com.shreyas.CloudDemo.service.interfaces;

import com.shreyas.CloudDemo.bean.UserBean;
import com.shreyas.CloudDemo.bean.UserProfilePicBean;
import org.apache.coyote.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserBean updateUser(String emailId, UserBean updatedUser) throws BadRequestException;

    boolean isExistingUserByEmail(String emailId);

    UserBean createUser(UserBean user) throws BadRequestException;

    UserBean findByEmail(String emailUd);

    UserProfilePicBean getUserProfilePicture(String emailId);

    UserProfilePicBean uploadUserProfilePicture(String emailId, MultipartFile file) throws BadRequestException;

    boolean deleteUserProfilePicture(String emailId);
}
