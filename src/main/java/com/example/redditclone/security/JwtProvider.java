package com.example.redditclone.security;

import com.example.redditclone.exceptions.SpringRedditException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtProvider {

  private KeyStore keyStore;

  @Value("${jwt.expiration.time}")
  private Long jwtExpirationInMillis;

  @PostConstruct
  public void init() {
    try {
      keyStore = KeyStore.getInstance("JKS");
      InputStream resourceAsStream = getClass().getResourceAsStream("/springblog.jks");
      keyStore.load(resourceAsStream, "secret".toCharArray());
    } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
      throw new SpringRedditException("Exception occurred while loading the key store", e);
    }
  }

  public String generateToken(Authentication authentication) {
    User principle = (User) authentication.getPrincipal();
    return Jwts.builder()
        .setSubject(principle.getUsername())
        .signWith(getPrivateKey())
        .setExpiration(Date.from(Instant.now().plusMillis(jwtExpirationInMillis)))
        .compact();
  }

  public String generateTokenWithUsername(String username) {
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(Date.from(Instant.now()))
        .signWith(getPrivateKey())
        .setExpiration(Date.from(Instant.now().plusMillis(jwtExpirationInMillis)))
        .compact();
  }

  public boolean validateToken(String jws) {
    Jwts.parserBuilder().setSigningKey(getPublicKey()).build().parseClaimsJws(jws);
    return true;
  }

  public String getUsernameFromJwt(String token) {
    Claims claims =
        Jwts.parserBuilder().setSigningKey(getPublicKey()).build().parseClaimsJws(token).getBody();
    return claims.getSubject();
  }

  private PrivateKey getPrivateKey() {
    try {
      return (PrivateKey) keyStore.getKey("springblog", "secret".toCharArray());
    } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
      throw new SpringRedditException(
          "Exception occurred while " + "retrieving public key from key store");
    }
  }

  private PublicKey getPublicKey() {
    try {
      return keyStore.getCertificate("springblog").getPublicKey();
    } catch (KeyStoreException e) {
      throw new SpringRedditException(
          "Error occurred while retrieving " + "public key from keystore. ", e);
    }
  }

  public Long getJwtExpirationInMillis() {
    return jwtExpirationInMillis;
  }
}
