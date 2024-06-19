package com.szbldb.exception;

import com.szbldb.pojo.Result;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result error(Exception e){
        log.error("捕获到异常", e);
        return Result.error("Something got failed.");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public Result expired(ExpiredJwtException ee){
        log.trace("用户登录失效", ee);
        return Result.error("Not_Login", 50007);
    }
}
