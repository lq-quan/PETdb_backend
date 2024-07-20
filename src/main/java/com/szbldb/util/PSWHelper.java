package com.szbldb.util;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Component
public class PSWHelper {

    @Value("${rsa.private-key}")
    private String initPrivateKey;

    private static String privateKey;

    @PostConstruct
    public void init(){
        privateKey = initPrivateKey;
    }

    /**
     *
     * @Description 检查密码明文强度
     * @param password 密码明文
     * @return boolean
     * @author Quan Li 2024/7/18 10:38
     **/
    public static boolean checkPswIfWeak(String password){
        boolean numFlag = false, charFlag = false;
        if(password.length() < 10) return true;
        for(char ch : password.toCharArray()){
            if((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')) charFlag = true;
            else if(ch >= '0' && ch <= '9') numFlag = true;
            else if(ch != ',' && ch != ' ') return true;
        }
        return !numFlag || !charFlag;
    }

    /**
     *
     * @Description 解密 RSA 密码
     * @param encoded 密码 RSA 密文
     * @return java.lang.String
     * @author Quan Li 2024/7/18 11:04
     **/
    public static String decodeRSAPsw(String encoded){
        byte[] encodedBytes = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(encodedBytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(spec);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encoded));
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
