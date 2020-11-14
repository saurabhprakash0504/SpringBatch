package com.sample.listners;

import com.sample.models.FlatFileRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.FlatFileParseException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
public class FlatFileListener {

    String skipFile;

    FlatFileListener(String skipFile, String skipFolder){
        this.skipFile = skipFolder +skipFile +".SKIP";
    }

    public void onSkipInRead(Throwable t) {
        if(t instanceof FlatFileParseException){
            FlatFileParseException flatFileParseException = (FlatFileParseException) t;
            writeSkipRecord(flatFileParseException.getInput());
        }
    }

    public void onSkipInProcess(FlatFileRecord item, Throwable t) {
        writeSkipRecord(item.getOriginalRecord());
    }

    public void writeSkipRecord(String input) {
        try(BufferedWriter fileWriter = Files.newBufferedWriter(Paths.get(skipFile), StandardOpenOption.CREATE, StandardOpenOption.APPEND)){
            fileWriter.append(input).append("\n");

        }catch (IOException e){
            log.error("Unable to write skipped line");
        }
    }


}
