package com.szbldb;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class SzblabApplicationTests {


    @Test
    void contextLoads(){
        System.out.println(3 / 2);
        log.error("I am handsome!");
    }


}
