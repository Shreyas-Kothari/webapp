package com.shreyas.CloudDemo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(
        name = "images",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"userId"})
        }
)
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ReadOnlyProperty
    @NotNull(message = "FileName is required in DB")
    private String fileName;

    @ReadOnlyProperty
    @NotNull(message = "Image URL is required in DB")
    private String url;

    @ReadOnlyProperty
    @NotNull(message = "uploadDate is required in DB")
    private LocalDateTime uploadDate=LocalDateTime.now();

    @NotNull(message = "UserID is required in DB")
    @ReadOnlyProperty
    private UUID userId;
}
