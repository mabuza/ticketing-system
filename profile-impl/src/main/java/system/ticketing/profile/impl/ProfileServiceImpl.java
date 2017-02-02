package system.ticketing.profile.impl;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import system.ticketing.profile.api.ProfileService;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Created by Ncube on 2/2/17.
 */
public class ProfileServiceImpl implements ProfileService {
    private static final Logger log = LoggerFactory.getLogger(ProfileServiceImpl.class);

    @Override
    public ServiceCall<NotUsed, String> test() {
        log.info("ProfileService is Up!!");
        return (request) -> completedFuture("ProfileService is Up!!");
    }
}
