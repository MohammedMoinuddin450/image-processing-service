package com.imageProcessing.demo.controller;


import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.imageProcessing.demo.Dtos.imageResponse;
import com.imageProcessing.demo.Dtos.transformReqdto;
import com.imageProcessing.demo.service.transformService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/transform")
@AllArgsConstructor
public class transformController {

    private transformService tService;

    @PostMapping("/changeformat")
    public ResponseEntity<?> changeFormat( @RequestBody MultipartFile file,
            @RequestParam("format") String format) throws IOException {

        byte[] convertedImage = tService.changeFormat(file, format);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("image/" + format));
        headers.setContentDispositionFormData("attachment", "converted_image." + format);
        return new ResponseEntity<>(convertedImage, headers, HttpStatus.OK);
    }

    @PostMapping("/images/{id}/transformations")
    public ResponseEntity<?> transformImage( @PathVariable String id, 
                                    @RequestBody transformReqdto transformations,
                                    Authentication authentication) throws IOException {
    imageResponse response = tService.transformImage(id, transformations, authentication);
    return ResponseEntity.ok(response);
    }
}