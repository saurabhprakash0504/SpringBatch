package com.sample.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;

import org.springframework.batch.core.BatchStatus;
import com.sample.tasklet.CheckFileName;
import com.sample.utils.FileUtils;

import jdk.internal.jline.internal.Log;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/batch")
@Slf4j
public class FlatFileController {
	
	private final ConfigurableApplicationContext ctx;
	
	@Value("${flatFiles.directories.input}")
	private String flatFileInputFolder;
	
	@Value("${flatFiles.directories.skip}")
	private String flatFileSkipFolder;
	
	@Value("${flatFiles.validInputFileNameExpression}")
	private String flatFileNameExpression;
	
	public FlatFileController(final ConfigurableApplicationContext ctx) {
		this.ctx=ctx;
	}
	
	@GetMapping("/triggerFlatFile")
	public void runFlatFile() throws ParseException, IOException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		JobLauncher jobLauncher = ctx.getBean(JobLauncher.class);
		Job pergeFileJob =ctx.getBean("purgeFileJob",Job.class);
		Job flatFileJob = ctx.getBean("flatFileJob", Job.class);
		
		JobExecution purgeFileExecution = jobLauncher.run(pergeFileJob, new JobParameters());
		BatchStatus purgeFileBatchStatus = purgeFileExecution.getStatus();
		
		checkFileName().run();
		final Resource[] resources = getSortedResources();
		
		for(final Resource resource : resources) {
			Log.debug("Running Job for file ");
			JobParameters jobParameters = getJobParam(resource);
			JobExecution flatFileJobExecution = jobLauncher.run(flatFileJob, jobParameters);
			org.springframework.batch.core.BatchStatus flatFileBatchStatus = flatFileJobExecution.getStatus();
		}
	}

	private JobParameters getJobParam(Resource resource) throws IOException {
		return new JobParametersBuilder().addString(
"fileUrl", resource.getURI().toString()).toJobParameters();
	}

	private Resource[] getSortedResources() throws ParseException, IOException {
		int startOfDate = 14;
		int endOfDate = 28;
		Resource[] sortedResources = FileUtils.sortFileByDateAndTime(startOfDate, endOfDate, getInputResources());
		
		for(final Resource resource : sortedResources) {
			Log.debug("fileName :: {} ", resource.getFilename());
		}
		
		return sortedResources;
	}

	private Resource[] getInputResources() throws IOException {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		return resolver.getResources("file:"+ flatFileInputFolder+"/*");
	}

	private CheckFileName checkFileName() {
		return new CheckFileName(flatFileInputFolder, flatFileSkipFolder, flatFileNameExpression);
	}
	
}
