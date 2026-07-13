package com.antonio.prog.service;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {
  private String id;
  private String nomFichier;
  private String email;
  private Instant createdAt;
  private String urlOriginal;
  private String urlNoirEtBlanc;
}
