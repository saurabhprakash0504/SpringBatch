package com.sample.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import javax.validation.ValidationException;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class CheckHeaderTrailer implements Tasklet {
    String filePathUri;
    String headerPattern;
    String trailerPattern;

    public CheckHeaderTrailer(String filePathUri, String headerPattern, String trailerPattern){
        this.filePathUri = filePathUri;
        this.headerPattern = headerPattern;
        this.trailerPattern = trailerPattern ;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        File file = new File(new URI(filePathUri));
        String firstLine = head(file);
        if(!firstLine.matches(headerPattern)){
            throw new ValidationException("Invalid file.");
        }

        String lastLine = tail(file);
        if(!lastLine.matches(trailerPattern)){
            throw new ValidationException("Invalid file. Incorrect trailer");
        }


        return null;
    }

    private String tail(File file) throws IOException {

        int zero = 0xA;
        try(RandomAccessFile fileHandler = new RandomAccessFile(file, "r")){
            long fileLength = fileHandler.length()-1 ;
            StringBuilder stringBuilder = new StringBuilder();

            for( long filePointer = fileLength; filePointer !=-1 ; filePointer --){
                fileHandler.seek(filePointer);
                final int readByte = fileHandler.readByte();
                if(readByte == zero){
                    if(filePointer == fileLength){
                        continue;
                    }
                    break;
                }stringBuilder.append((char) readByte);
            }
            return stringBuilder.reverse().toString();
        }
    }

    // @Nullable
    private String head(File file) throws IOException {
        try(BufferedReader fileHandler = Files.newBufferedReader(Paths.get(file.toURI()))){
            return fileHandler.readLine();
        }
    }
}
