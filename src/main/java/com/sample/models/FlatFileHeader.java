package com.sample.models;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class FlatFileHeader extends FlatFileRecord{

    @Pattern(regexp = "00", message = "recordType should be 00")
    @NotNull
    private String recordType;

    @Pattern(regexp = "\\d{4}\\d{2}\\d{2}", message = "date should be YYYYMMDD")
    @NotNull
    private String date;

    @Pattern(regexp = "[0-9]{6}", message = "sequenceNumber should be digit")
    @NotNull
    private String sequenceNumber;



}
