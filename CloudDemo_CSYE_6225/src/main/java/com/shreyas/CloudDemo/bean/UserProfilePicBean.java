package com.shreyas.CloudDemo.bean;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserProfilePicBean {
    private UUID id;
    private String fileName;
    private String url;
    private LocalDateTime uploadDate;
    private UUID userId;
}
