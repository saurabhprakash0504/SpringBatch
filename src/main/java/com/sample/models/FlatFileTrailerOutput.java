package com.sample.models;

import lombok.Data;

@Data
public class FlatFileTrailerOutput {

    String recordType = "22";

    String recordCount;

    public String toOutputFormat(){
        return String.format("%s%s", recordType,recordCount);
    }

}
