package com.shreyas.CloudDemo.service.implementations;

import com.shreyas.CloudDemo.bean.UserBean;
import com.shreyas.CloudDemo.entity.User;
import com.shreyas.CloudDemo.repository.UserRepo;
import com.shreyas.CloudDemo.service.interfaces.UserService;
import com.shreyas.CloudDemo.utility.GenericBeanMapper;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper mapper;

    @Transactional
    public UserBean createUser(UserBean user) throws BadRequestException {

        if (user.getAccount_created() != null || user.getAccount_updated() != null)
            throw new BadRequestException("Account create/update date cannot be set by user.");

        if (isExistingUserByEmail(user.getEmail())) {
            throw new BadRequestException("User already exists with email id: " + user.getEmail());
        }

        User u = GenericBeanMapper.map(user, User.class, mapper);

        u.setPassword(passwordEncoder.encode(u.getPassword()));
        u = userRepo.save(u);
        return GenericBeanMapper.map(u, UserBean.class, mapper);
    }

    public boolean isExistingUserByEmail(String email) {
        return userRepo.findByEmail(email).isPresent();
    }


    public UserBean findByEmail(String emailUd) {
        Optional<User> u = userRepo.findByEmail(emailUd);
        if (u.isPresent()) {
            return GenericBeanMapper.map(u, UserBean.class, mapper);
        }
        return null;
    }

    @Transactional
    public UserBean updateUser(String emailId, UserBean updatedUser) throws BadRequestException {
        if (updatedUser.getAccount_created() != null || updatedUser.getAccount_updated() != null)
            throw new BadRequestException("Account create/update date cannot be set by user.");

        User existingUser = userRepo.findByEmail(emailId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        userRepo.save(existingUser);
        return GenericBeanMapper.map(existingUser, UserBean.class, mapper);
    }
}
