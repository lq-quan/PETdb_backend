package com.szbldb;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class SzblabApplicationTests {

    @Test
    void contextLoads(){
        String password = "123456";
        String sha = DigestUtils.sha256Hex(password + "petdatabase");
        System.out.println(sha);
        String encoded = BCrypt.hashpw(sha, BCrypt.gensalt());
        System.out.println(encoded);
        System.out.println(encoded.length());
        System.out.println(BCrypt.checkpw(sha, "$2a$10$KmaEPQ8yTXzDqw6zi6E7jet8IIwbuCCQFAiSUKvZOpgBOo9A/fdxK"));
    }


}
