package system.ticketing.registration.impl;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import system.ticketing.registration.api.RegistrationService;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Created by Ncube on 2/2/17.
 */
public class RegistrationServiceImpl implements RegistrationService {
    private static final Logger log = LoggerFactory.getLogger(RegistrationServiceImpl.class);

    @Override
    public ServiceCall<NotUsed, String> test() {
        log.info("RegistrationService is Up!!");
        return (request) -> completedFuture("RegistrationService is Up!!");
    }
}
