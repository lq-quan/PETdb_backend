package com.szbldb.pojo.licensePojo;

import lombok.Data;

import java.util.List;

@Data
public class SubmissionList {
    private int total;
    List<Submission> items;
}
