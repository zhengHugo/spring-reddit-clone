package com.example.redditclone.security;

import com.example.redditclone.exceptions.SpringRedditException;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

@Service
public class JwtProvider {

    private KeyStore keyStore;

    @PostConstruct
    public void init() {
        try {
            keyStore = KeyStore.getInstance("JKS");
            InputStream resourceAsStream =
                getClass().getResourceAsStream("/springblog.jks");
            keyStore.load(resourceAsStream, "secret".toCharArray());
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new SpringRedditException(
                "Exception occurred while loading the key store", e);
        }
    }

    public String generateToken(Authentication authentication) {
        User principle = (User) authentication.getPrincipal();
        return Jwts.builder().setSubject(principle.getUsername())
            .signWith(getPrivateKey()).compact();
    }


    private PrivateKey getPrivateKey() {
        try {
            return (PrivateKey) keyStore
                .getKey("springblog", "secret".toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new SpringRedditException("Exception occurred while " +
                "retrieving public key from key store");
        }
    }
}
