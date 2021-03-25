package com.obj.nc.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class BaseRestReceiver<REQUEST_T, RESPONSE_T> {
    
    protected List<REQUEST_T> requests = new ArrayList<>();
    
    public RESPONSE_T receive(REQUEST_T request) {
        requests.add(request);
        return createDummyResponse();
    }
    
    protected abstract RESPONSE_T createDummyResponse();
    
    public boolean waitForIncomingRequests(long timeout, int requestCount) {
        final CountDownLatch waitObject = createNewWaitObject(requestCount);
        final long endTime = System.currentTimeMillis() + timeout;
        while (waitObject.getCount() > 0) {
            final long waitTime = endTime - System.currentTimeMillis();
            if (waitTime < 0L) {
                return waitObject.getCount() == 0;
            }
            try {
                waitObject.await(waitTime, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // Continue loop, in case of premature interruption
            }
        }
        return waitObject.getCount() == 0;
    }
    
    public List<REQUEST_T> getAndRemoveAllRequests() {
        List<REQUEST_T> allRequests = new ArrayList<>(requests);
        requests.removeAll(allRequests);
        return allRequests;
    }
    
    public void reset() {
        requests = new ArrayList<>();
    }
    
    private CountDownLatch createNewWaitObject(int requestCount) {
        final int existingCount = requests.size();
        if (existingCount >= requestCount) {
            return new CountDownLatch(0); // Requires no count down, therefore not added to notification list
        }
        return new CountDownLatch(requestCount - existingCount);
    }
    
}
