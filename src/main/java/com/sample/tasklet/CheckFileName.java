package com.sample.tasklet;

import java.io.File;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckFileName {
	
	String flatFileInputFolder;
	
	String flatFileSkipFolder;
	
	String flatFileNameExpression;
	
	public CheckFileName(String flatFileInputFolder, String flatFileSkipFolder, String flatFileNameExpression) {
		this.flatFileInputFolder = flatFileInputFolder;
		this.flatFileNameExpression = flatFileNameExpression;
		this.flatFileSkipFolder = flatFileSkipFolder;
	}
	
	public void run() {
		Pattern pattern = Pattern.compile(flatFileNameExpression);
		for(File file : Objects.requireNonNull(new File(flatFileInputFolder).listFiles())) {
			String fileName = file.getName();
			Matcher matcher = pattern.matcher(fileName);
			if(matcher.matches()) {
				log.info("fileName matches {} ",file.getName());
			}else {
				log.warn("Invalid fileName {} ", file.getName());
				file.renameTo(moveFileInSkipDir(flatFileSkipFolder, file));
			}
		}
	}

	private File moveFileInSkipDir(String flatFileSkipFolder, File file) {
		return new File(flatFileSkipFolder + file.getName());
	}

}
