package com.sample.listners;

import com.sample.models.FlatFileDetailOutput;
import com.sample.models.FlatFileRecord;
import org.springframework.batch.core.SkipListener;

public class FlatFileSkipListner extends FlatFileListener implements SkipListener<FlatFileRecord, FlatFileDetailOutput> {

    public FlatFileSkipListner(String skipFile, String skipFolder) {
        super(skipFile, skipFolder);
    }

    @Override
    public void onSkipInWrite(FlatFileDetailOutput item, Throwable t) {
        writeSkipRecord(item.getOriginalRecord());
    }

}
