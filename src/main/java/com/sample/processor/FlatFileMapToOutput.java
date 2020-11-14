package com.sample.processor;

import com.sample.models.FlatFileDetailOutput;
import com.sample.models.FlatFileDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FlatFileMapToOutput implements ItemProcessor<FlatFileDetails , List<FlatFileDetailOutput>> {
    @Override
    public List<FlatFileDetailOutput> process(FlatFileDetails item) throws Exception {

        List<FlatFileDetailOutput> list = new ArrayList<>();
        FlatFileDetailOutput output = new FlatFileDetailOutput();

        output.setRecordInfo(item.getRecordType());
        output.setOriginalRecord(item.getOriginalRecord());
        output.setAddressInfo(item.getCity()+item.getPostCode());

        list.add(output);
        return list;
    }
}
