package com.example.redditclone.service;

import com.example.redditclone.exceptions.SpringRedditException;
import com.example.redditclone.model.RefreshToken;
import com.example.redditclone.repositories.RefreshTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  public RefreshToken generateRefreshToke() {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setToken(UUID.randomUUID().toString());
    refreshToken.setCreatedDate(Instant.now());

    return refreshTokenRepository.save(refreshToken);
  }

  public void validateRefreshToken(String token) {
    refreshTokenRepository
        .findByToken(token)
        .orElseThrow(() -> new SpringRedditException("Invalid refresh token"));
  }

  public void deleteRefreshToken(String token) {
    refreshTokenRepository.deleteByToken(token);
  }
}
