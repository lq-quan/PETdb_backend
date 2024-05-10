package com.szbldb.pojo.logPojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Operation {
    private Integer id;
    private LocalDateTime time;
    private String operation;
    private String operator;

    public Operation(LocalDateTime time, String operation) {
        this.time = time;
        this.operation = operation;
    }

    public Operation() {
    }

    @Override
    public String toString() {
        return "Operation{" +
                "id=" + id +
                ", time=" + time +
                ", operation='" + operation + '\'' +
                ", operator='" + operator + '\'' +
                '}';
    }
}
