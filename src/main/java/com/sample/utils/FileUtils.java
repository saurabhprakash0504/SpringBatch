package com.sample.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.core.io.Resource;

import jdk.internal.jline.internal.Log;

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
			Log.debug(resource.getFilename());
		}
		
		return sortedResources;
		
	}

}
