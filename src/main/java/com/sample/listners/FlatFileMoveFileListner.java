package com.sample.listners;

import ch.qos.logback.core.util.FileUtil;
import com.sample.utils.FileUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileDeleteStrategy;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@Component
public class FlatFileMoveFileListner implements JobExecutionListener {

    String skipPath;

    String inputArchievePath;

    String outputPath;

    String processingPath;

    String fileUrl;

    String fileName;

    String ffSequenceFile;

    @Autowired
    FileUtils fileUtils;

    public FlatFileMoveFileListner(String skipPath, String inputArchievePath, String outputPath, String processingPath, String fileUrl, String ffSequenceFile) throws MalformedURLException {
        this.skipPath = skipPath;
        this.inputArchievePath = inputArchievePath;
        this.outputPath = outputPath;
        this.processingPath = processingPath;
        this.fileUrl = fileUrl;
        this.ffSequenceFile = ffSequenceFile;
        this.fileName = new UrlResource(fileUrl).getFilename();
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    @SneakyThrows
    @Override
    public void afterJob(JobExecution jobExecution) {
        String exitCode = jobExecution.getExitStatus().getExitCode();

        int skipCount = 0;
        Collection<StepExecution> stepExecutionCollection = jobExecution.getStepExecutions();
        for(StepExecution stepExecution : stepExecutionCollection){
            skipCount += stepExecution.getSkipCount();
        }

        if(exitCode.equals(ExitStatus.FAILED.getExitCode())){
            log.error("Moving file to skip directory");

            File processingFile = new File(processingPath);

            for(File file : Objects.requireNonNull(processingFile.listFiles())){
                if(file.exists()){
                    FileDeleteStrategy.FORCE.delete(file);
                }
            }

        }else if(exitCode.equals(ExitStatus.COMPLETED.getExitCode())){
            long outBoundSequenceNumber = getOutboundFileSeqNo();
            String outputFilePath = processingPath + "FlatFileOutput"+ outBoundSequenceNumber;
            FileUtils.moveFileToDir(outputFilePath, outputPath);

            FileUtils.moveFileToDir(fileUrl, inputArchievePath);

            if(skipCount > 0){
               // String skipFilePath = processingPath + fileName + ".SKIP";
                String skipFilePath = processingPath + fileName;
             //   FileUtils.moveFileToDir(skipFilePath, skipPath);
            }
            fileUtils.setSequenceNumber(ffSequenceFile, "outbound.sequence", outBoundSequenceNumber+1);
        }else {
            log.error("exception while moving the file");
        }

    }

    private long getOutboundFileSeqNo() throws ConfigurationException {
        return fileUtils.getSequenceNumber(ffSequenceFile, "outbound.sequence");
    }
}
