package com.sample.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "flatfiles.file-layout")
public class FlatFileLayout extends FileLayout{
}
