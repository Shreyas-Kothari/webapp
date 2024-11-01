package com.shreyas.CloudDemo.service.implementations;

import com.shreyas.CloudDemo.bean.UserBean;
import com.shreyas.CloudDemo.bean.UserProfilePicBean;
import com.shreyas.CloudDemo.entity.Image;
import com.shreyas.CloudDemo.entity.User;
import com.shreyas.CloudDemo.repository.ImageRepo;
import com.shreyas.CloudDemo.repository.UserRepo;
import com.shreyas.CloudDemo.service.S3StorageService;
import com.shreyas.CloudDemo.service.interfaces.UserService;
import com.shreyas.CloudDemo.utility.GenericBeanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper mapper;
    private final ImageRepo imageRepo;
    private final S3StorageService s3Service;

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
        log.info("User saved successfully !!");
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
        log.info("User updated successfully!!");
        return GenericBeanMapper.map(existingUser, UserBean.class, mapper);
    }

    @Override
    public UserProfilePicBean getUserProfilePicture(String emailId) {
        UUID userId = findByEmail(emailId).getId();
        Optional<Image> dbResult = imageRepo.findByUserId(userId);
        return dbResult.map(image -> GenericBeanMapper.map(image, UserProfilePicBean.class, mapper)).orElse(null);
    }

    @Override
    public UserProfilePicBean uploadUserProfilePicture(String emailId, MultipartFile file) throws BadRequestException {
        if (file != null) {
            List<String> acceptedExtensions = Arrays.asList(".jpg", ".png", ".jpeg");

            String fileName = file.getOriginalFilename();
            String fileExtension = fileName.substring(fileName.lastIndexOf("."));

            if (!acceptedExtensions.contains(fileExtension.toLowerCase())) {
                throw new MultipartException("Invalid file extension. Only " + acceptedExtensions + " are accepted.");
            }

            User user = userRepo.findByEmail(emailId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if (imageRepo.findByUserId(user.getId()).isEmpty()) {
                String path = user.getId().toString();
                String fileUrl = s3Service.saveFile(path, fileName, file);
                if (!fileUrl.isBlank()) {
                    Image imageEntity = new Image();
                    imageEntity.setUrl(fileUrl);
                    imageEntity.setFileName(fileName);
                    imageEntity.setUserId(user.getId());
                    imageRepo.save(imageEntity);
                    log.info("User profile picture uploaded successfully!!");
                    return GenericBeanMapper.map(imageEntity, UserProfilePicBean.class, mapper);
                }
            } else {
                throw new BadRequestException("User profile picture already exists");
            }
        }
        return null;
    }

    @Override
    public boolean deleteUserProfilePicture(String emailId) {
        User user = userRepo.findByEmail(emailId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Optional<Image> dbResult = imageRepo.findByUserId(user.getId());
        if (dbResult.isPresent()) {
            Image image = dbResult.get();
            boolean deleteFromS3 = s3Service.deleteFile(image.getUrl());
            if (deleteFromS3) {
                // If file deleted from S3 successfully, delete from database as well.
                imageRepo.deleteById(image.getId());
                log.info("File deleted from S3 and database successfully !!");
                return true;
            } else
                return false;
        }
        return false;
    }


}
