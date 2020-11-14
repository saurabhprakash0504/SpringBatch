package com.sample.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FileUtils {

	public static Resource[] sortFileByDateAndTime(int startOfDate, int endOfDate, Resource[] inputResources) throws ParseException {
		final SortedMap<Date, Resource> sortedMap = new TreeMap<>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss", Locale.ENGLISH);
		for(Resource resource : inputResources) {
			String fileName = resource.getFilename();
			if(null != fileName && fileName.length() >= endOfDate) {
				String dateString = fileName.substring(startOfDate, endOfDate);
				Date parDate = dateFormat.parse(dateString);
				sortedMap.put(parDate, resource);
			}
		}
		
		final ArrayList<Resource> arrayList = new ArrayList<>();
		for(final Map.Entry<Date, Resource> map : sortedMap.entrySet()) {
			arrayList.add(map.getValue());
		}
		
		Resource[] sortedResources = arrayList.toArray(new Resource[0]);
		
		for(Resource resource : sortedResources) {
			log.debug(resource.getFilename());
		}
		
		return sortedResources;
		
	}

	public static void createDirectory(Path path) throws IOException {
		if(path.toFile().exists()){
			log.info("folder already present {}", path);
		}else{
			Files.createDirectories(path);
			log.info("Created path {} ",path.toAbsolutePath());
		}
	}

	public static void moveFileToDir(String fileName, String outputFilePath) throws Exception {
		Path file = Paths.get(fileName.replaceFirst("^file:/", ""));
		String newPath = outputFilePath + file.getFileName();
		try{
			Files.move(file, Paths.get(newPath), StandardCopyOption.REPLACE_EXISTING);
		}catch (IOException exception){
			throw new Exception("Unable to move file");
		}
	}

	public long getSequenceNumber(final String sequenceFilePath, String key) throws ConfigurationException {
		PropertiesConfiguration configuration = new PropertiesConfiguration(sequenceFilePath);
		return configuration.getLong(key);
	}

	public void setSequenceNumber(String flatFileSequenceFile, String key, long value) throws ConfigurationException {
		PropertiesConfiguration configuration = new PropertiesConfiguration(flatFileSequenceFile);
		configuration.setProperty(key,value);
		configuration.save();
	}
}
