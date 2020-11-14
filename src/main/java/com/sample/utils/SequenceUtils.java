package com.sample.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
@Slf4j
public class SequenceUtils {

    public void createAndInitializeSequenceProperties(final String filepath) throws ConfigurationException, IOException {
        File file = new File(filepath);
        if(file.createNewFile()){
            log.info("sequence.properties creation successful");
            final PropertiesConfiguration conf = new PropertiesConfiguration(filepath);
            conf.setProperty("inbound.sequence",0);
            conf.setProperty("outbound.sequence",0);
            conf.save();
        }else {
            log.warn("Error while creating. File already exist.");
        }
    }

}
