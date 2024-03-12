package com.szbldb.pojo;

import lombok.Data;

@Data
public class Result {
    private Integer code;
    private String msg;
    private Object data;

    public Result(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result() {
    }

    public static Result error(String msg, Integer code){
        return new Result(code, "error", msg);
    }

    public static Result success(Object data){
        return new Result(20000, "success", data);
    }

    public static Result success(){
        return new Result(20000, "success", null);
    }

    public static Result error(String msg){
        return new Result(0, "error", msg);
    }
}
