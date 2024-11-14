package com.shreyas.CloudDemo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(
        name = "verification_token",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = "token")
        }
)
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    private String token;
    private LocalDateTime expiryDate;
    private boolean isUsed=false;
    private LocalDateTime createdDate;

    public VerificationToken() {
        this.token = UUID.randomUUID().toString();
        this.createdDate = LocalDateTime.now();
    }
}
