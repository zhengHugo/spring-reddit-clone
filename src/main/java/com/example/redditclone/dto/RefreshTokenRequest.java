package com.example.redditclone.dto;

import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
public class RefreshTokenRequest {
  @NotBlank private String refreshToken;
  private String username;
}
