package com.shreyas.CloudDemo.service.interfaces;

import com.shreyas.CloudDemo.bean.UserBean;
import org.apache.coyote.BadRequestException;

public interface UserService {
    UserBean updateUser(String emailId, UserBean updatedUser) throws BadRequestException;

    boolean isExistingUserByEmail(String emailId);

    UserBean createUser(UserBean user) throws BadRequestException;

    UserBean findByEmail(String emailUd);
}
