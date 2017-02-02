package system.ticketing.processmodule.impl;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import system.ticketing.processmodule.api.ProcessModuleService;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Created by Ncube on 2/2/17.
 */
public class ProcessModuleServiceImpl implements ProcessModuleService {
    private static final Logger log = LoggerFactory.getLogger(ProcessModuleServiceImpl.class);

    @Override
    public ServiceCall<NotUsed, String> test() {
        log.info("ProcessModuleService is Up!!");
        return (request) -> completedFuture("ProcessModuleService is Up!!");
    }
}
