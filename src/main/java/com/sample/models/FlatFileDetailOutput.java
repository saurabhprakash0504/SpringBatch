package com.sample.models;

import lombok.Data;

@Data
public class FlatFileDetailOutput {

    private String recordInfo;

    private int businessType = 99;

    private String addressInfo;

    private String originalRecord;

   public String toOutputFormat(){
        return String.format("%s%d%s%s", recordInfo,businessType,addressInfo,originalRecord);
    }

}
