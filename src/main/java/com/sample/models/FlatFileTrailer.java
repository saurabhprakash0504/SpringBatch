package com.sample.models;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class FlatFileTrailer extends FlatFileRecord {

    @Pattern(regexp = "93", message = "Should be 93")
    @NotNull
    private String dataType;

    @Pattern(regexp = "[0-9]{12}", message = "Should be digit")
    @NotNull
    private String recordCount;

}
