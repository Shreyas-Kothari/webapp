package com.shreyas.CloudDemo.service.implementations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shreyas.CloudDemo.bean.EmailRequest;
import com.shreyas.CloudDemo.bean.UserBean;
import com.shreyas.CloudDemo.bean.UserProfilePicBean;
import com.shreyas.CloudDemo.entity.Image;
import com.shreyas.CloudDemo.entity.User;
import com.shreyas.CloudDemo.entity.VerificationToken;
import com.shreyas.CloudDemo.repository.ImageRepo;
import com.shreyas.CloudDemo.repository.UserRepo;
import com.shreyas.CloudDemo.repository.VerificationTokenRepo;
import com.shreyas.CloudDemo.service.S3StorageService;
import com.shreyas.CloudDemo.service.SNSMailService;
import com.shreyas.CloudDemo.service.interfaces.UserService;
import com.shreyas.CloudDemo.utility.GenericBeanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
    private final SNSMailService snsService;
    private final VerificationTokenRepo verificationTokenRepo;
    @Value("${spring.application.baseURL}")
    private String baseUrl;
    @Value("${verification.token.expiration.minutes}")
    private long expirationTime;

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

        VerificationToken token = new VerificationToken();
        token.setUser(u);
        token.setExpiryDate(token.getCreatedDate().plusMinutes(expirationTime));
        token = verificationTokenRepo.save(token);
        log.info("User verification token created successfully");

        String verificationTokenLink = baseUrl + "/v1/users/verify?user=" + u.getId() + "&token=" + token.getToken();

        log.info("Verification link is: {}", verificationTokenLink);

        String message = buildEmail(u.getFirstName(), verificationTokenLink);

        sendVerificationMail(u.getEmail(), message);

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

    @Override
    public String verifyUser(String user, String token) {
        Optional<VerificationToken> verificationToken = verificationTokenRepo.findByToken(token);
        if (verificationToken.isPresent()) {
            User userEntity = verificationToken.get().getUser();
            if (userEntity != null && userEntity.getId().toString().equals(user) &&
                    verificationToken.get().getExpiryDate().isAfter(LocalDateTime.now())) {
                verificationToken.get().setUsed(true);
                userEntity.setIsEnabled(true);
                userRepo.save(userEntity);

                return verificationSuccess(userEntity.getFirstName());
            }
        }
        return verificationFailed();
    }

    private void sendVerificationMail(String emailId, String message) {
        EmailRequest request = new EmailRequest(emailId, "Verify the email address", message);
        try {
            boolean response = snsService.publishMailRequestToTopic(request);
            if (response) {
                log.info("EmailID verification mail published successfully!!");
            } else {
                log.error("Failed to publish emailID verification mail.");
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. Please click on the below link to activate your account: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Activate Now</a> </p></blockquote>\n Link will expire in " +expirationTime+ " minutes. <p>See you soon</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }

    private String verificationFailed() {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Account verification</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi  User,</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> You failed to register <p>Please check the link again if its expired</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }

    private String verificationSuccess(String name) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. \n Your registration was successful. <p>Join with us and start using our website.</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }
}
