package com.mainlineclean.app.utils;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

@Component
@ConfigurationProperties(prefix = "spring.security.hmac")
public class HMacSigner {
    @Setter private String algo;

    @Setter private String usersecret;
    @Setter private String adminsecret;

    private SecretKeySpec keySpecUser;
    private SecretKeySpec keySpecAdmin;

    @PostConstruct
    private void init() {
        byte[] userSecretBytes = Base64.getDecoder().decode(usersecret);
        byte[] adminSecretBytes = Base64.getDecoder().decode(adminsecret);
        this.keySpecUser = new SecretKeySpec(userSecretBytes, algo);
        this.keySpecAdmin = new SecretKeySpec(adminSecretBytes, algo);
    }

    public String getSignature(boolean isAdmin) {
        String uuid = (isAdmin ? "admin" : "user") + UUID.randomUUID();
        byte[] signature = computeHmac(uuid, isAdmin);
        return uuid + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
    }

    public boolean verify(String clientToken, boolean isAdmin) {
        String[] parts = clientToken.split("\\.");
        if (parts.length != 2) {
            return false;
        }

        String uuid = parts[0];
        String signature = parts[1];

        byte[] signatureBytes;
        try {signatureBytes = Base64.getUrlDecoder().decode(signature);} catch (Exception e) {
            return false;
        }
        byte[] expectedSignature = computeHmac(uuid, isAdmin);
        return MessageDigest.isEqual(expectedSignature, signatureBytes);
    }

    // this converts the uuid to the Hmac uuid version based off the secret
    private byte[] computeHmac(String data, boolean isAdmin) {
        try {
            Mac mac = Mac.getInstance(algo);

            if(isAdmin) mac.init(keySpecAdmin);
            else mac.init(keySpecUser);

            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC computation failed", e);
        }
    }
}