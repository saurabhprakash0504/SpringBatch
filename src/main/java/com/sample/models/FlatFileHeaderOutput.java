package com.sample.models;

import com.sample.utils.FileUtils;
import lombok.Data;

@Data
public class FlatFileHeaderOutput {

    private String recordType = "11";

    private String fileSequenceNo;

}
