package com.szbldb;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@Slf4j
@SpringBootTest
class SzblabApplicationTests {

    @Test
    void contextLoads(){
        String psw = "111111";
        System.out.println(DigestUtils.sha256Hex(psw + "petdatabase"));
    }


}
