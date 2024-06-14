package com.szbldb;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class SzblabApplicationTests {

    //private final Logger log = LoggerFactory.getLogger(getClass());

    @Test
    void contextLoads(){
        System.out.println(3 / 2);
    }


}
