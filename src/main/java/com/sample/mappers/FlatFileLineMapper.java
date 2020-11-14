package com.sample.mappers;

import com.sample.exceptions.RecordException;
import com.sample.models.FlatFileDetails;
import com.sample.models.FlatFileHeader;
import com.sample.models.FlatFileRecord;
import com.sample.models.FlatFileTrailer;
import com.sample.properties.FlatFileLayout;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
public class FlatFileLineMapper extends  FlatFileRecordLineMapper implements LineMapper<FlatFileRecord> {

    @Autowired
    FlatFileLayout flatFileLayout;

    @Value("#{jobParameters['fileUrl']}")
    String  fileName;

    @Getter
    @Setter
    int detailRecordCount;

    @Override
    public FlatFileRecord mapLine(String line, int lineNumber) throws Exception {
        FlatFileRecord outputRecord;
        if(line.matches(flatFileLayout.getHeaderPattern())){
            outputRecord = mapHeader(line,fileName,lineNumber);
        }else if( line.matches(flatFileLayout.getDetailPattern())){
            detailRecordCount++;
            outputRecord = mapDetail(line,fileName,lineNumber);
        }else if( line.matches(flatFileLayout.getTrailerPattern())){
            outputRecord = mapTrailer(line,fileName,lineNumber);
        }else {
            detailRecordCount++;
            throw new RecordException("Invalid line found ");
        }
        return outputRecord;
    }

    private FlatFileRecord mapTrailer(String line, String fileName, int lineNumber) {
        FlatFileTrailer trailer = new FlatFileTrailer();
        flatFileLayout.getTrailerField().forEach(field -> this.parseValue(line,field,trailer));
        trailer.setFileName(fileName);
        trailer.setLineNumber(lineNumber);
        trailer.setOriginalRecord(line);
        return trailer;
    }

    private FlatFileRecord mapDetail(String line, String fileName, int lineNumber) {
        FlatFileDetails details = new FlatFileDetails();
        flatFileLayout.getDetailField().forEach(field -> this.parseValue(line,field,details));
        details.setFileName(fileName);
        details.setLineNumber(lineNumber);
        details.setOriginalRecord(line);
        return details;
    }

    private FlatFileRecord mapHeader(String line, String fileName, int lineNumber) {
        FlatFileHeader header = new FlatFileHeader();
        flatFileLayout.getHeaderField().forEach(field -> this.parseValue(line,field,header));
        header.setFileName(fileName);
        header.setLineNumber(lineNumber);
        header.setOriginalRecord(line);
        return header;
    }
}
