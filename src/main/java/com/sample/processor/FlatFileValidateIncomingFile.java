package com.sample.processor;

import com.sample.exceptions.RecordException;
import com.sample.mappers.FlatFileLineMapper;
import com.sample.models.FlatFileDetails;
import com.sample.models.FlatFileHeader;
import com.sample.models.FlatFileRecord;
import com.sample.models.FlatFileTrailer;
import com.sample.properties.FlatFileLayout;
import com.sample.utils.FileUtils;
import com.sample.utils.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.ConfigurationException;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

@StepScope
@Slf4j
@Component
public class FlatFileValidateIncomingFile implements ItemProcessor<FlatFileRecord, FlatFileDetails> {

    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

    private boolean headerProcessed;

    private boolean trailerProcessed;

    @Autowired
    private FlatFileLayout flatFileLayout;

    @Autowired
    ValidationUtils validationUtils;

    @Autowired
    FlatFileLineMapper flatFileLineMapper;

    @Autowired
    FileUtils fileUtils;

    @Value("${flatFiles.sequenceFile}")
    private String flatFileSequenceFile;

    @Override
    public FlatFileDetails process(FlatFileRecord record) throws Exception {
        Validator validator = validatorFactory.getValidator();
        FlatFileDetails flatFileDetails = null;

        if(record instanceof FlatFileHeader){
            processHeader(record, validator);
        }else if (record instanceof FlatFileDetails){
            flatFileDetails = processDetail(record,validator);
        }else if (record instanceof FlatFileTrailer){
            processTrailer(record,validator);
        }else {
            log.info("Unknown record type.");
            throw new RecordException("Invalid Record");
        }

        return flatFileDetails;
    }

    private void processHeader(FlatFileRecord record, Validator validator) throws ConfigurationException {
        log.info("validating header");
        FlatFileHeader flatFileHeader = (FlatFileHeader) record;
        ValidationUtils.checkHeaderProcessedAndOnFirstLine(flatFileHeader.getLineNumber(),headerProcessed);
        ValidationUtils.checkRecordLength(flatFileHeader.getOriginalRecord(), Integer.parseInt(flatFileLayout.getHeaderLength()));
        validationUtils.checkForPattern(record, flatFileLayout.getHeaderField(), validator);

        long inboundSeq = fileUtils.getSequenceNumber(flatFileSequenceFile,"inbound.sequence");

        if(Long.parseLong(flatFileHeader.getSequenceNumber() )!= inboundSeq){
            throw new RecordException( "Inbound sequence number does not match.");
        }

        fileUtils.setSequenceNumber(flatFileSequenceFile,"inbound.sequence", inboundSeq+1);

        headerProcessed = true;
    }

    private FlatFileDetails processDetail(FlatFileRecord record, Validator validator){
        log.info("File {} line {} - Detail validator",record.getFileName(), record.getLineNumber());
        FlatFileDetails details = (FlatFileDetails) record;
        ValidationUtils.checkRecordLength(details.getOriginalRecord(),Integer.parseInt(flatFileLayout.getDetailLength()));
        ValidationUtils.checkIfHeaderProcessed(headerProcessed);
        validationUtils.checkForPattern(details,flatFileLayout.getDetailField(),validator);
        log.debug("File {} line {} - record is valid", details.getFirstName(),details.getLineNumber());
        return details;
    }

    private void processTrailer(FlatFileRecord record, Validator validator){
        log.info("File {} line {} trailer validation ", record.getFileName(),record.getLineNumber());
        ValidationUtils.checkIfTrailerAlreadyProcessed(trailerProcessed);
        FlatFileTrailer trailer = (FlatFileTrailer) record;
        ValidationUtils.checkRecordLength(trailer.getOriginalRecord(), Integer.parseInt(flatFileLayout.getTrailerLength()));
        validationUtils.checkForPattern(trailer,flatFileLayout.getTrailerField(),validator);
        int expectedRecord = Integer.parseInt(trailer.getRecordCount());
        int detailRecord = flatFileLineMapper.getDetailRecordCount();

        if(expectedRecord != detailRecord){
            throw new ValidationException("expected and actual record count doesnt match");
        }
        flatFileLineMapper.setDetailRecordCount(0);
        trailerProcessed = true;
    }
}
