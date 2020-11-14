package com.sample.controller;

import com.sample.utils.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.io.IOException;
import java.text.ParseException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
//@TestPropertySource(properties = {})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FlatFileControllerTest {

    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    FileUtils fileUtils;

    @Autowired
    @Qualifier(value = "flatFileJob")
    Job job;

    @Autowired
    FlatFileController flatFileController;

    @InjectMocks
    JobExecution jobExecution;

    @Before
    public void setUp(){

    }

    @Test
    public void runFlatFileTest() throws JobInstanceAlreadyCompleteException, ParseException, JobExecutionAlreadyRunningException, IOException, JobRestartException, JobParametersInvalidException {
        flatFileController.runFlatFile();
        Assert.assertEquals(job.getName(), "flatFileJob");
        //Assert.assertNotNull(flatFileController.ge);

    }


}
