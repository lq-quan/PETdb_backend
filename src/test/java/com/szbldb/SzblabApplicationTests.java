package com.szbldb;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;


import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


@Slf4j
@SpringBootTest
class SzblabApplicationTests {
    @Value("${rsa.public-key}")
    private String pubKey;

    @Value("${rsa.private-key}")
    private String priKey;

    @Test
    void contextLoads() throws Exception {
//        String psw = "111111";
//        String encoded = DigestUtils.sha256Hex(psw + "petdatabase");
//        System.out.println(encoded);
//        System.out.println(BCrypt.checkpw(encoded, "$2a$10$zEgQwiruw62ZzFuCJurICewighsYbiN.ZtkW6apnAY6n3eJUGU./u"));
//        System.out.println(priKey.length());
//        System.out.println(pubKey.length());
//        byte[] pubKeyBytes = Base64.getDecoder().decode(pubKey);
        byte[] priKeyBytes = Base64.getDecoder().decode(priKey);
//        X509EncodedKeySpec spec = new X509EncodedKeySpec(pubKeyBytes);
        PKCS8EncodedKeySpec spec1 = new PKCS8EncodedKeySpec(priKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        PublicKey publicKey = keyFactory.generatePublic(spec);
        PrivateKey privateKey = keyFactory.generatePrivate(spec1);
//        String plain = "Hello, RSA AAA ?? !@!~sd ABQWER!";
//        Cipher cipher = Cipher.getInstance("RSA");
//        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//        byte[] encryptedBytes = cipher.doFinal(plain.getBytes());
//        String encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);
        Cipher cipher1 = Cipher.getInstance("RSA");
        cipher1.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decrypted = cipher1.doFinal(Base64.getDecoder().decode("W8PXeaTYXze4qkRzPymWXhc6W+c4QKR09OwJ4BEMo7ISp1yEalA9UtiQMkuKYhd/lI7RT0I0jWCLM53Z3zHl8VpgZJIZIV+Ss7y5MPtXNVP/rJeoaV5ywV2lZC2/aqB6eKh0Q+yT9lZ9zVUlL3f/MTTv0r4/Dixp+AOY4KTKP+/f9bAbYAK30XFXM1Or9ZTIC2euaUngC5xCDaj7YsX44Krh+Yl6/iYpCL8AWCROSana6jJz1ObBKJKshfGVzn2meXqEJwslZO7JeC98tahUlyAXPeOanwWAVTEn8WCU6okWQlkX+AIP5ypiHeefYOBarqAq/lvUHQrVfUHm0TXUxA=="));
        System.out.println(new String(decrypted));
    }


}
