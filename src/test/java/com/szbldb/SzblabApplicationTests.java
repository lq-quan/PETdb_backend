package com.szbldb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class SzblabApplicationTests {

    @Test
    void contextLoads() throws UnknownHostException {
        List<Integer> list = new ArrayList<>();
        list.add(123);
        list.add(456);
        System.out.println(list);
    }


}
