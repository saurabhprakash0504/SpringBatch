package com.sample.models;

import lombok.Data;

import javax.validation.constraints.Pattern;

@Data
public class FlatFileDetails extends FlatFileRecord{

    @Pattern(regexp = "01", message = "Record type should be 01")
    private String recordType;

    @Pattern(regexp = "[A-z ]{0,9}", message = "firstName should be alpha numberic")
    private String firstName;

    @Pattern(regexp = "[A-z ]{0,9}", message = "lastName should be alpha numberic")
    private String lastName;

    @Pattern(regexp = "[0-9]{1,2}", message = "postcode should be number")
    private String postCode;

    @Pattern(regexp = "[A-z ]{0,5}", message = "city should be alpha numberic")
    private String city;


}
