package com.antonio.prog.endpoint.rest.controller;

import com.antonio.prog.service.ImageResponse;
import com.antonio.prog.service.ImageService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
public class ImageController {
  private final ImageService imageService;

  @PostMapping("/images")
  public ResponseEntity<ImageResponse> uploadImage(
      @RequestParam String email, @RequestParam("file") MultipartFile file) {
    var response = imageService.uploadImage(email, file);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/images")
  public ResponseEntity<List<ImageResponse>> getAllImages() {
    return ResponseEntity.ok(imageService.getAllImages());
  }
}
