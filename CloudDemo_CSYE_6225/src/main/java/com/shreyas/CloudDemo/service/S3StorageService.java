package com.shreyas.CloudDemo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService {
    private final S3Client s3Client;
    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String saveFile(String path, String filename, MultipartFile file) {
        try {
            if (file.isEmpty())
                throw new IllegalArgumentException("File must not be empty");
            Map<String, String> metadata = new HashMap<>();
            metadata.put("Content-Length", String.valueOf(file.getSize()));
            metadata.put("Content-Type", file.getContentType());

            String filePath = path + "/" + filename;
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .metadata(metadata)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return bucketName + "/" + filePath;
        } catch (SdkClientException e) {
            log.info("SdkClientException:{}", e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (S3Exception serviceException) {
            log.info("S3Exception: {}", serviceException.getMessage(), serviceException);
            throw serviceException;
        } catch (IOException e) {
            log.error("IOException: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public boolean deleteFile(String filePath) {
        try {
            // Check if the object exists using headObject
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath.substring(filePath.indexOf("/") + 1))   // remove the bucket name from the path
                    .build();

            // If the object doesn't exist, this call will throw a 404 error
            s3Client.headObject(headRequest);

            // Delete the object if it exists
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath.substring(filePath.indexOf("/") + 1))     // remove the bucket name from the path
                    .build();

            s3Client.deleteObject(deleteRequest);
            return true;
        } catch (NoSuchKeyException e) {
            log.error("NoSuchKeyException: {}", e.getMessage(), e);
            throw new RuntimeException("NoSuchKeyException: " + e.getMessage());
        } catch (S3Exception e) {
            log.error("Error deleting S3 object: {}", e.awsErrorDetails().errorMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Exception: Error deleting S3 object: {}", e.getMessage(), e);
            return false;
        }
    }
}
