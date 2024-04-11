package com.szbldb.exception;

import com.szbldb.pojo.Result;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result error(Exception e){
        e.printStackTrace();
        return Result.error("Something got failed.");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public Result expired(ExpiredJwtException ee){
        ee.printStackTrace();
        return Result.error("Not_Login", 50007);
    }
}
