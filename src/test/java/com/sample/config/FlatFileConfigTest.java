package com.sample.config;

import com.sample.mappers.FlatFileLineMapper;
import com.sample.mappers.FlatFileRecordLineMapper;
import com.sample.processor.FlatFileValidateIncomingFile;
import com.sample.properties.FlatFileLayout;
import com.sample.utils.FileUtils;
import com.sample.utils.SequenceUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

@SpringBootTest
@TestPropertySource(properties = {"flatFiles.directories.input=src/main/resources/input/",
        "flatFiles.directories.skip=src/main/resources/skip/",
        "flatFiles.directories.output=src/main/resources/output/",
        "flatFiles.directories.processing=src/main/resources/processing/",
        "flatFiles.directories.sequence=src/main/resources/sequence/",
        "flatFiles.directories.inputArchieve=src/main/resources/inputArcieve/",
        "flatFiles.directories.outputArchieve=src/main/resources/outputArcheieve/",
        "flatFiles.sequenceFile=src/main/resources/sequence/sequence.properties"})
@RunWith(SpringRunner.class)
@SpringBatchTest
@EnableAutoConfiguration
@ContextConfiguration(classes ={
        FlatFileConfig.class,
        FlatFileRecordLineMapper.class,
        FlatFileLineMapper.class,
        FileUtils.class,
        SequenceUtils.class,
        FlatFileLayout.class,
        RestTemplate.class,
        FlatFileValidateIncomingFile.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FlatFileConfigTest {

    private static String testInputFile = "src/test/resources/sample/input/FlatFile000001";
    private static String validInputFile = "src/test/resources/input/FlatFile000001";
    private static String sequenceFile = "src/test/resources/sequence/sequence.properties";

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    public static void copyFile(File from, File to) throws IOException {
        org.apache.commons.io.FileUtils.copyFile(from, to);
    }

    @Before
    public void setUp() throws IOException {
         copyFile(new File(validInputFile), new File(testInputFile));
        copyFile(new File(sequenceFile), new File("src/test/resources/sequence/ff/sequence.properties"));
    }

    @After
    public void cleanUp(){
        jobRepositoryTestUtils.removeJobExecutions();

    }

    private JobParameters defaultJobParameters(){
        JobParametersBuilder parametersBuilder = new JobParametersBuilder();
        parametersBuilder.addString("fileUrl", new File(testInputFile).toURI().toString());
        return parametersBuilder.toJobParameters();
    }

    @Test
    public void fileFlow1JobSuccessful() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();
        Assert.assertEquals(actualJobInstance.getJobName(),"flatFileJob");
    }

}
