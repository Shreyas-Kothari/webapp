package com.shreyas.CloudDemo.repository;

import com.shreyas.CloudDemo.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImageRepo extends JpaRepository<Image, UUID> {
    Optional<Image> findByUserId(UUID userId);
}
