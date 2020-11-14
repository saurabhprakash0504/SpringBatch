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
		super();
		this.ctx=ctx;
	}
	
	@GetMapping("/triggerFlatFile")
	public void runFlatFile() throws ParseException, IOException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		JobLauncher jobLauncher = ctx.getBean(JobLauncher.class);
	//	JobParameters jobParameters = getDefaultJobParameter();
	//	Job purgeFileJob =ctx.getBean("purgeFileJob",Job.class);
		Job flatFileJob = ctx.getBean("flatFileJob", Job.class);
		
	//	JobExecution purgeFileExecution = jobLauncher.run(purgeFileJob, new JobParameters());
	//	BatchStatus purgeFileBatchStatus = purgeFileExecution.getStatus();
		
		checkFileName().run();
		//final Resource[] resources = getSortedResources();
		Resource[] resources = getInputResources();

		for(final Resource resource : resources) {
			log.debug("Running Job for file ");
			JobParameters jobParameters = getJobParam(resource);
			JobExecution flatFileJobExecution = jobLauncher.run(flatFileJob, jobParameters);
			org.springframework.batch.core.BatchStatus flatFileBatchStatus = flatFileJobExecution.getStatus();
		}
	}

	public JobParameters getDefaultJobParameter(){
		return new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();
	}

	public JobParameters getJobParam(Resource resource) throws IOException {
		return new JobParametersBuilder()
				.addLong("time", System.currentTimeMillis())
				.addString("fileUrl", resource.getURI().toString())
				.toJobParameters();
	}

	private Resource[] getSortedResources() throws ParseException, IOException {
		int startOfDate = 14;
		int endOfDate = 28;
		Resource[] sortedResources = FileUtils.sortFileByDateAndTime(startOfDate, endOfDate, getInputResources());
		
		for(final Resource resource : sortedResources) {
			log.debug("fileName :: {} ", resource.getFilename());
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
