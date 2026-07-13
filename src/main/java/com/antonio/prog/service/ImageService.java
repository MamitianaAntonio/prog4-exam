package com.antonio.prog.service;

import static java.io.File.createTempFile;
import static java.util.UUID.randomUUID;

import com.antonio.prog.file.bucket.BucketComponent;
import com.antonio.prog.repository.ImageRepository;
import com.antonio.prog.repository.model.ImageEntity;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
@Slf4j
public class ImageService {
  private final ImageRepository imageRepository;
  private final BucketComponent bucketComponent;

  @SneakyThrows
  public ImageResponse uploadImage(String email, MultipartFile file) {
    var id = randomUUID().toString();
    var originalFilename = file.getOriginalFilename();
    var now = Instant.now();

    var tempFile = createTempFile(id, "-" + originalFilename);
    file.transferTo(tempFile);

    var originalKey = "images/" + id + "/original/" + originalFilename;
    bucketComponent.upload(tempFile, originalKey);
    tempFile.delete();

    imageRepository.save(
        ImageEntity.builder()
            .id(id)
            .nomFichier(originalFilename)
            .email(email)
            .createdAt(now)
            .build());

    convertToBlackAndWhite(id, originalFilename, originalKey);

    var originalUrl = bucketComponent.presign(originalKey, Duration.ofMinutes(5)).toString();

    return ImageResponse.builder()
        .id(id)
        .nomFichier(originalFilename)
        .email(email)
        .createdAt(now)
        .urlOriginal(originalUrl)
        .build();
  }

  @Async
  @SneakyThrows
  public void convertToBlackAndWhite(String id, String filename, String originalKey) {
    var downloaded = bucketComponent.download(originalKey);

    var bwImage = toBlackAndWhite(downloaded);
    downloaded.delete();

    var bwKey = "images/" + id + "/bw/" + filename;
    var bwFile = createTempFile(id + "-bw", "-" + filename);
    ImageIO.write(bwImage, filename.endsWith(".png") ? "png" : "jpg", bwFile);

    bucketComponent.upload(bwFile, bwKey);
    bwFile.delete();

    log.info("B&W image uploaded to S3: {}", bwKey);
  }

  private BufferedImage toBlackAndWhite(File imageFile) throws IOException {
    var colorImage = ImageIO.read(imageFile);
    var bwImage = new BufferedImage(
        colorImage.getWidth(), colorImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
    var gray = bwImage.getGraphics();
    gray.drawImage(colorImage, 0, 0, Color.WHITE, null);
    gray.dispose();
    return bwImage;
  }

  public List<ImageResponse> getAllImages() {
    return imageRepository.findAll().stream()
        .map(this::toResponse)
        .toList();
  }

  private ImageResponse toResponse(ImageEntity entity) {
    var id = entity.getId();
    var filename = entity.getNomFichier();

    var originalKey = "images/" + id + "/original/" + filename;
    var bwKey = "images/" + id + "/bw/" + filename;

    var originalUrl = bucketComponent.presign(originalKey, Duration.ofMinutes(5)).toString();
    var bwUrl = bucketComponent.presign(bwKey, Duration.ofMinutes(5)).toString();

    return ImageResponse.builder()
        .id(id)
        .nomFichier(filename)
        .email(entity.getEmail())
        .createdAt(entity.getCreatedAt())
        .urlOriginal(originalUrl)
        .urlNoirEtBlanc(bwUrl)
        .build();
  }
}
