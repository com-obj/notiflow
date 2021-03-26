package com.obj.nc.functions.sources;

import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.services.BaseRestReceiver;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public abstract class BaseRestReceiverSourceSupplier<REQUEST_T> extends SourceSupplierAdapter<List<REQUEST_T>> {
    
    protected final BaseRestReceiver<REQUEST_T, ?> restReceiver;
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(List<REQUEST_T> requests) {
        return Optional.empty();
    }
    
    @Override
    protected List<REQUEST_T> execute() {
        return restReceiver.getAndRemoveAllRequests();
    }
    
}
