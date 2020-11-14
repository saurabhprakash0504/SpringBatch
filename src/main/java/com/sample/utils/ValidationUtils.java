package com.sample.utils;

import com.sample.exceptions.RecordException;
import com.sample.models.FlatFileRecord;
import com.sample.properties.FileLayout;
import com.sample.properties.FlatFileLayout;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ValidationUtils {

    public static void checkHeaderProcessedAndOnFirstLine(final int lineNo, boolean headerProcessed){
        final int firstLine = 1;
        if(headerProcessed){
            throw new RecordException("header already processed.");
        }else if( lineNo != firstLine){
            throw new RecordException("Invalid header. Header is not on the first line.");
        }
    }

    public static void checkRecordLength(final String record, int expectedLength){
        if (expectedLength != record.length()){
            throw new RecordException("Invalid length");
        }
    }

    public static void checkIfHeaderProcessed(boolean headerProcessed) {
        if(!headerProcessed){
            log.error("Header not processed");
            throw new RecordException("header not processed");
        }
    }

    public static void checkIfTrailerAlreadyProcessed(boolean trailerProcessed) {
        if(trailerProcessed){
            log.error("footer already processed");
            throw new RecordException("Record exception - Trailer already processed");
        }
    }

    public void checkForPattern(FlatFileRecord record, List<FileLayout.Field> fieldList, Validator validator){
        Set<ConstraintViolation<FlatFileRecord>> violations = validator.validate(record);
        if(!violations.isEmpty()){
            for(ConstraintViolation<FlatFileRecord> violation : violations){
                boolean fieldMandatory = ! fieldList.stream().filter(field -> field.getName().equals(violation.getPropertyPath().toString()))
                        .filter(FileLayout.Field::isMandatory)
                        .collect(Collectors.toList())
                        .isEmpty();

                if(fieldMandatory || !StringUtils.isBlank(violation.getInvalidValue().toString())){
                    log.warn("Violations: {} {}",violation.getPropertyPath(), violation.getMessage());
                    throw new RecordException(String.format("Invalid Record - validation failed on line [%s]", record.getLineNumber()));
                }
            }
        }
    }

}
