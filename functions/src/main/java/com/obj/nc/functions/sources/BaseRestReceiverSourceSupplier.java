package com.obj.nc.functions.sources;

import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.services.BaseRestReceiver;

import java.util.List;
import java.util.Optional;

public abstract class BaseRestReceiverSourceSupplier extends SourceSupplierAdapter<List<?>> {
    
    @Override
    protected Optional<PayloadValidationException> checkPreCondition(List<?> requests) {
        return Optional.empty();
    }
    
    @Override
    protected List<?> execute() {
        return getRestReceiver().getAndRemoveAllRequests();
    }
    
    protected abstract BaseRestReceiver<?, ?> getRestReceiver();
    
}
