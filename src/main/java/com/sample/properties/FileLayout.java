package com.sample.properties;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FileLayout {

    List<Field> headerField = new ArrayList<Field>();

    List<Field> detailField = new ArrayList<Field>();

    List<Field> trailerField = new ArrayList<Field>();

    List<Field> awardField = new ArrayList<Field>();

    String headerPattern;

    String detailPattern;

    String trailerPattern;

    String headerLength;

    String detailLength;

    String trailerLength;

    @Data
    public static class Field{

        private String name;

        private int start;

        private int end;

        private boolean mandatory;
    }
}
