package com.obj.nc.testmode.functions.sources;

import com.obj.nc.domain.message.Message;
import com.obj.nc.functions.sources.SourceMicroService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Supplier;

import static com.obj.nc.testmode.functions.sources.GreenMailReceiverMicroService.SERVICE_NAME;

@Service(SERVICE_NAME)
@Log4j2
@ConditionalOnProperty(value = "testmode.enabled", havingValue = "true")
public class GreenMailReceiverMicroService extends SourceMicroService<List<Message>, GreenMailReceiverSourceSupplier>
        implements Supplier<Flux<List<Message>>> {

    public static final String SERVICE_NAME = "receiveGreenMailMessages";

    @Autowired
    private GreenMailReceiverSourceSupplier supplier;

    @Override
    public Flux<List<Message>> get() {
        return super.executeSourceService().get();
    }

    @Override
    public GreenMailReceiverSourceSupplier getSourceSupplier() {
        return supplier;
    }


}
