package com.sample.writer;

import com.sample.models.FlatFileDetailOutput;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.FlatFileItemWriter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class FlatFileCustomItemWriter extends FlatFileItemWriter<List<FlatFileDetailOutput>> {

    @Setter
    public static int counter;

    public void write(List<? extends List<FlatFileDetailOutput>> detailRecordList) throws IOException {
        OutputState state = this.getOutputState();
        for(final List<FlatFileDetailOutput> flatFileDetailOutputs : detailRecordList){
            for( FlatFileDetailOutput flatFileDetailOutput : flatFileDetailOutputs){
                state.write(flatFileDetailOutput.toOutputFormat()+ this.lineSeparator);
                counter++;
            }
        }
    }

}
