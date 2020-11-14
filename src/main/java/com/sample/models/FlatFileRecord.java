package com.sample.models;

import lombok.Data;

@Data
public class FlatFileRecord {

    private String fileName;

    private int lineNumber;

    private String recordType;

    private String originalRecord;
}
