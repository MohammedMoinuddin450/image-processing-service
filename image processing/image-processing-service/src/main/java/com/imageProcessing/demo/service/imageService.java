package com.imageProcessing.demo.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import com.imageProcessing.demo.Dtos.imageResponse;
import com.imageProcessing.demo.model.Image;
import com.imageProcessing.demo.model.User;
import com.imageProcessing.demo.repos.imageRepo;
import com.imageProcessing.demo.repos.userRepo;

import lombok.AllArgsConstructor;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@AllArgsConstructor
public class imageService {

    private final S3Client amazonS3Client;
    private final imageRepo imagesRepository;
    private final userRepo userRepository;
    
    @Value("{BUCKET_NAME}")
    private String bucketName;

    @Value("{R2_ACCOUNTID}")
    private String r2AccountId;
   
    public imageResponse uploadImage(MultipartFile file, Authentication authentication) throws S3Exception, AwsServiceException, SdkClientException, IOException{
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }

        String username = authentication.getName();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        amazonS3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        String fileUrl = "https://" + bucketName + "." + r2AccountId + ".r2.cloudflarestorage.com/" + fileName;

        Image uploadedImage = Image.builder()
                .r2url(fileUrl)
                .filename(fileName)
                .filetype(file.getContentType())
                .fileSize(file.getSize())
                .fileExtension(fileExtension)
                .createdDate(LocalDate.now())
                .updatedDate(LocalDate.now())
                .userid(user.getUserid())
                .build();

        imagesRepository.save(uploadedImage);
        imageResponse response=new imageResponse();
        response.setR2Url(fileUrl);
        response.setFileName(fileName);
        response.setFileType(fileExtension);
        response.setFileSize(file.getSize());
        
        return response;
    }

    public imageResponse getImageData(String id) {
        Image image = imagesRepository.findById(Integer.parseInt(id))
                .orElseThrow(() -> {
                    return new IllegalArgumentException("Image not found");
                });
        imageResponse imageData=new imageResponse();
        imageData.setImageId(image.getImageId());
        imageData.setR2Url(image.getR2url());
        imageData.setFileName(image.getFilename());
        imageData.setFileType(image.getFiletype());
        imageData.setFileSize(image.getFileSize());

        return imageData;
    }

    public void deleteImage(String id, Authentication authentication) throws IOException {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }

        int userId = (int) authentication.getCredentials();
        Image image = imagesRepository.findById(Integer.parseInt(id))
                .orElseThrow(() -> {

                    return new IllegalArgumentException("Image not found");
                });

        if (image.getUserid()==userId) {
            throw new SecurityException("User is not authorized to delete this image");
        }

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(image.getFilename())
                .build();

        amazonS3Client.deleteObject(deleteObjectRequest);
        imagesRepository.delete(image);
    }
}
