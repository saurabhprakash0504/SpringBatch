package com.sample.config;

import com.sample.exceptions.RecordException;
import com.sample.listners.FlatFileMoveFileListner;
import com.sample.listners.FlatFileSkipListner;
import com.sample.mappers.FlatFileLineMapper;
import com.sample.models.FlatFileDetailOutput;
import com.sample.models.FlatFileHeaderOutput;
import com.sample.models.FlatFileRecord;
import com.sample.models.FlatFileTrailerOutput;
import com.sample.processor.FlatFileMapToOutput;
import com.sample.processor.FlatFileValidateIncomingFile;
import com.sample.properties.FlatFileLayout;
import com.sample.tasklet.CheckHeaderTrailer;
import com.sample.utils.FileUtils;
import com.sample.utils.SequenceUtils;
import com.sample.writer.FlatFileCustomItemWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.ConfigurationException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.*;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.retry.RetryException;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Configuration
@EnableBatchProcessing
@Slf4j
@PropertySource("classpath:application-${spring.profiles.active}.yml")
public class FlatFileConfig {

    public static final String JOB_PARAMETERS_FILE_URL = "#{jobParameters['fileUrl']}";

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Autowired
    FlatFileLineMapper flatFileLineMapper;

    @Value("${flatFiles.directories.input}")
    private String ffInputFolder;

    @Value("${flatFiles.directories.output}")
    private String ffOutputFolder;

    @Value("${flatFiles.directories.skip}")
    private String ffSkipFolder;

    @Value("${flatFiles.directories.processing}")
    private String ffProcessingFolder;

    @Value("${flatFiles.directories.inputArchieve}")
    private String ffInputArchieveFolder;

    @Value("${flatFiles.directories.outputArchieve}")
    private String ffOutputArchieveFolder;

    @Value("${flatFiles.directories.sequence}")
    private String ffSequenceFolder;

    @Value("${flatFiles.sequenceFile}")
    private String ffSequenceFile;

    @Value("${flatFiles.retryLimit}")
    private int retryLimit;

    @Autowired
    FileUtils fileUtils;

    @Autowired
    SequenceUtils sequenceUtils;

    @Autowired
    FlatFileValidateIncomingFile flatFileValidateIncomingFiles;

    @Autowired
    FlatFileLayout flatFileLayout;

    @PostConstruct
    public void createDir() throws IOException, ConfigurationException {
        createDirectory(ffInputFolder);
        createDirectory(ffOutputFolder);
        createDirectory(ffSkipFolder);
        createDirectory(ffProcessingFolder);
        createDirectory(ffInputArchieveFolder);
        createDirectory(ffOutputArchieveFolder);
        createDirectory(ffSequenceFolder);
        sequenceUtils.createAndInitializeSequenceProperties(ffSequenceFile);
    }

    @Bean
    @StepScope
    FlatFileItemReader<FlatFileRecord> flatFileReadFromFile(final @Value(JOB_PARAMETERS_FILE_URL) String fileName) throws MalformedURLException {
        return new FlatFileItemReaderBuilder<FlatFileRecord>()
                .name("FlatFileRecordItemReader")
                .resource(new UrlResource(fileName))
                .lineMapper(flatFileLineMapper)
                .encoding(FlatFileItemReader.DEFAULT_CHARSET)
                .beanMapperStrict(false)
                .strict(false)
                .build();
    }
    @Bean
    @StepScope
    FlatFileSkipListner flatFileSkipListner(final @Value(JOB_PARAMETERS_FILE_URL) String fileName) throws MalformedURLException {
        return new FlatFileSkipListner(ffProcessingFolder, new UrlResource(fileName).getFilename());
    }

    @Bean
    @StepScope
    @Nullable
    public ItemProcessor flatFileValidateIncomingFile(){return flatFileValidateIncomingFiles;}

    @Bean
    ItemProcessor flatFileMapToOutput() {return  new FlatFileMapToOutput();}

    @Bean
    @StepScope
    public Tasklet flatFileCheckHeaderAndTrailer(@Value(JOB_PARAMETERS_FILE_URL) String fileName){
        return new CheckHeaderTrailer(fileName, flatFileLayout.getHeaderPattern(), flatFileLayout.getTrailerPattern());
    }

    @Bean
    @Qualifier("flatFileJob")
    @Primary
    public Job flatFileJob(Step flatFileProcessRecords, Step checkHeaderTrailerStep, FlatFileMoveFileListner flatFileMoveFileListner){
        return jobBuilderFactory.get("flatFileJob").incrementer(new RunIdIncrementer())
                .start(checkHeaderTrailerStep)
                .next(flatFileProcessRecords)
                .listener(flatFileMoveFileListner)
                .build();
    }

    public long getOutboundFileSeqNo() throws ConfigurationException {
        return fileUtils.getSequenceNumber(ffSequenceFile, "outbound.sequence");
    }

    @Bean
    @JobScope
    //@StepScope
    public FlatFileMoveFileListner flatFileMoveFileListner(@Value(JOB_PARAMETERS_FILE_URL) String fileUrl) throws MalformedURLException {
        return new FlatFileMoveFileListner(ffSkipFolder,ffInputArchieveFolder, ffOutputFolder, ffProcessingFolder, fileUrl, ffSequenceFile);
    }

    @Bean
    Step checkHeaderTrailerStep(Tasklet flatFileCheckHeaderTrailerTasklet){
        return stepBuilderFactory.get("Check Header Trailer")
                .tasklet(flatFileCheckHeaderTrailerTasklet)
                .build();
    }

    @Bean
    public Step flatFileProcessRecords(ItemReader<FlatFileRecord> flatFileRecordItemReader,
                                       FlatFileSkipListner flatFileSkipListner) throws ConfigurationException {
        return stepBuilderFactory.get("step1")
                .<FlatFileRecord, FlatFileDetailOutput>chunk(1)
                .reader(flatFileRecordItemReader)
                .processor(compose(flatFileValidateIncomingFile(), flatFileMapToOutput()))
                .faultTolerant()
                .skipLimit(2)
                .skip(FlatFileParseException.class)
                .skip(RecordException.class)
                .skip(NullPointerException.class)
                .retryLimit(retryLimit)
                .retry(RetryException.class)
                .listener(flatFileSkipListner)
                .writer(flatFileWriter())
                .build();
    }

    @Bean
    @StepScope
    @AfterChunk
    public FlatFileItemWriter flatFileWriter() throws ConfigurationException {

        FlatFileCustomItemWriter writer = new FlatFileCustomItemWriter();
        FlatFileCustomItemWriter.setCounter(0);
        long outVoundSequenceNumber = getOutboundFileSeqNo();
        FlatFileHeaderOutput flatFileHeaderOutput = new FlatFileHeaderOutput();
        flatFileHeaderOutput.setFileSequenceNo(String.valueOf(outVoundSequenceNumber));
        String outBoundFileName = ffProcessingFolder + "/" + "FlatFileOutput" + String.valueOf(outVoundSequenceNumber);
        writer.setResource(new FileSystemResource(outBoundFileName));
        writer.setLineAggregator(new PassThroughLineAggregator());
        writer.setHeaderCallback(new FlatFileHeaderCallBack(flatFileHeaderOutput));
        writer.setFooterCallback(new FlatFileFooterCallback());
        return writer;
    }

    public static class FlatFileHeaderCallBack implements FlatFileHeaderCallback{
        FlatFileHeaderOutput flatFileHeaderOutput;

        FlatFileHeaderCallBack(FlatFileHeaderOutput flatFileHeaderOutput){
            this.flatFileHeaderOutput = flatFileHeaderOutput;
        }

        public void writeHeader(Writer writer) throws IOException {
            String strinbBuilder = flatFileHeaderOutput.getRecordType()
                    +flatFileHeaderOutput.getFileSequenceNo();
            writer.write(strinbBuilder);
        }
    }

    public static class FlatFileFooterCallback implements org.springframework.batch.item.file.FlatFileFooterCallback{

        @Override
        public void writeFooter(Writer writer) throws IOException {
            FlatFileTrailerOutput flatFileTrailerOutput = new FlatFileTrailerOutput();
            String stringBuilder = flatFileTrailerOutput.getRecordType() + FlatFileCustomItemWriter.counter;
            writer.write(stringBuilder);
        }
    }

    private CompositeItemProcessor<FlatFileRecord, FlatFileDetailOutput> compose(ItemProcessor<?,?>... itemProcessors) {
        return new CompositeItemProcessorBuilder<FlatFileRecord, FlatFileDetailOutput>()
                .delegates(Arrays.asList(itemProcessors))
                .build();
    }

    public static void createDirectory(final String dir) throws IOException {
        final Path path= Paths.get(dir);
        FileUtils.createDirectory(path);
    }

}
