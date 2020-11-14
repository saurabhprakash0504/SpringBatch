package com.sample.mappers;

import com.sample.models.FlatFileRecord;
import com.sample.properties.FileLayout;
import com.sample.properties.FlatFileLayout;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.stereotype.Component;

@StepScope
@Component
@Slf4j
public class FlatFileRecordLineMapper {

    public void parseValue(String line, FileLayout.Field field, FlatFileRecord record){
        if(line.length() >= field.getEnd()){
            String value = line.substring(field.getStart() -1, field.getEnd());
            DirectFieldAccessor accessor = new DirectFieldAccessor(record);
            accessor.setPropertyValue(field.getName(),value.replace("\u0000",""));
        }
    }
}
