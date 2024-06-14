package com.szbldb.exception;

import com.szbldb.pojo.Result;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());
    @ExceptionHandler(Exception.class)
    public Result error(Exception e){
        log.error("捕获到异常", e);
        return Result.error("Something got failed.");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public Result expired(ExpiredJwtException ee){
        log.info("用户登录失效");
        return Result.error("Not_Login", 50007);
    }
}
