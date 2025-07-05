package com.imageProcessing.demo.service;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import com.imageProcessing.demo.Dtos.imageResponse;
import com.imageProcessing.demo.Dtos.transformReqdto;
import com.imageProcessing.demo.model.Image;
import com.imageProcessing.demo.repos.imageRepo;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@AllArgsConstructor
@Transactional
public class transformService {
    private imageRepo imageRepo;
    private S3Client amazonS3Client;

    @Value("{BUCKET_NAME}")
    private String bucketName;

    @Value("{R2_ACCOUNTID}")
    private String r2AccountId;


    public byte[] changeFormat(MultipartFile file, String format) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !isSupportedImageFormat(contentType)) {
            throw new IllegalArgumentException("Unsupported image format. Supported formats are JPEG, JPG, PNG, GIF, BMP, WBMP, and WebP.");
        }
        BufferedImage inputImage = ImageIO.read(file.getInputStream());
        if (inputImage == null) {
            throw new IOException("Failed to read image. The image might be corrupted or in an unsupported format.");
        }
        BufferedImage convertedImage = new BufferedImage(
                inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        convertedImage.createGraphics().drawImage(inputImage, 0, 0, Color.WHITE, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean success = ImageIO.write(convertedImage, format, baos);
        if (!success) {
            throw new IOException("Failed to write image in the specified format: " + format);
        }

        return baos.toByteArray();
    }

    private boolean isSupportedImageFormat(String contentType) {
        return contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/gif") ||
            contentType.equals("image/bmp") ||
            contentType.equals("image/wbmp") ||
            contentType.equals("image/webp");
    }


    public imageResponse transformImage(String id, transformReqdto transformations, Authentication authentication) throws IOException {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }

        Integer userId = (Integer) authentication.getCredentials();
        Image image = imageRepo.findById(Integer.parseInt(id))
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        if (image.getUserid()!=(userId)) {
            throw new SecurityException("User is not authorized to transform this image");
        }

        // Downloading image from R2
        File tempFile = File.createTempFile("image", "." + image.getFileExtension());
        try {
            amazonS3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key(image.getFilename()).build(), ResponseTransformer.toFile(tempFile));
        } catch (Exception e) {
            throw new IOException("Failed to download image from S3", e);
        }

        // Loading image using ImageJ
        ImagePlus img = IJ.openImage(tempFile.getAbsolutePath());
        ImageProcessor processor = img.getProcessor();

        // Resize
        if (transformations.getResize() != null) {
            Integer width = transformations.getResize().getWidth();
            Integer height = transformations.getResize().getHeight();
            if (width != null && height != null) {
                processor = processor.resize(width, height);
            }
        }

        // Rotate
        if (transformations.getRotate() != null) {
            processor.rotate(transformations.getRotate());
        }

        // Crop
        if (transformations.getCrop() != null) {
            transformReqdto.Crop crop = transformations.getCrop();
            processor.setRoi(crop.getX(), crop.getY(), crop.getWidth(), crop.getHeight());
            processor = processor.crop();
        }

        // Flip (Vertical)
        if (Boolean.TRUE.equals(transformations.getFlip())) {
            processor.flipVertical();
        }

        // Mirror (Horizontal)
        if (Boolean.TRUE.equals(transformations.getMirror())) {
            processor.flipHorizontal();
        }

        // Filters
        transformReqdto.Filters filters = transformations.getFilters();
        if (filters != null) {
            if (Boolean.TRUE.equals(filters.getGrayscale())) {
                processor = processor.convertToByte(true);
            }

            if (Boolean.TRUE.equals(filters.getSepia())) {
                applySepiaFilter(processor);
            }
        }

        String format = transformations.getFormat() != null ? transformations.getFormat() : image.getFileExtension();
        String transformedFileName = UUID.randomUUID().toString() + "." + format;
        File transformedFile = new File(tempFile.getParent(), transformedFileName);
        IJ.save(new ImagePlus("", processor), transformedFile.getAbsolutePath());

        amazonS3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(transformedFileName)
                        .contentType("image/" + format)
                        .build(),
                RequestBody.fromFile(transformedFile)
        );

        Image transformedImage = Image.builder()
                .r2url("https://" + bucketName + "." + r2AccountId + ".r2.cloudflarestorage.com/" + transformedFileName)
                .filename(transformedFileName)
                .filetype("image/" + format)
                .fileSize(transformedFile.length())
                .fileExtension(format)
                .createdDate(LocalDate.now())
                .updatedDate(LocalDate.now())
                .userid(userId)
                .build();

        imageRepo.save(transformedImage);

        // Generate presigned URL
        S3Presigner presigner = S3Presigner.create();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(transformedFileName)
                        .build())
                .build();

        String presignedUrl = presigner.presignGetObject(presignRequest).url().toString();

        imageResponse response = new imageResponse();
        response.setR2Url(transformedImage.getR2url());
        response.setFileName(transformedImage.getFilename());
        response.setFileType(transformedImage.getFiletype());
        response.setFileSize(transformedImage.getFileSize());

        tempFile.delete();
        transformedFile.delete();

        return response;
    }

    private void applySepiaFilter(ImageProcessor processor) {
        int width = processor.getWidth();
        int height = processor.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] rgb = processor.getPixel(x, y, (int[]) null);

                int tr = (int) (0.393 * rgb[0] + 0.769 * rgb[1] + 0.189 * rgb[2]);
                int tg = (int) (0.349 * rgb[0] + 0.686 * rgb[1] + 0.168 * rgb[2]);
                int tb = (int) (0.272 * rgb[0] + 0.534 * rgb[1] + 0.131 * rgb[2]);

                rgb[0] = Math.min(255, tr);
                rgb[1] = Math.min(255, tg);
                rgb[2] = Math.min(255, tb);

                processor.putPixel(x, y, rgb);
            }
        }
    }

}