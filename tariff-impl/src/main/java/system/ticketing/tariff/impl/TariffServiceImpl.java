package system.ticketing.tariff.impl;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import system.ticketing.tariff.api.TariffService;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Created by Ncube on 2/2/17.
 */
public class TariffServiceImpl implements TariffService {
    private static final Logger log = LoggerFactory.getLogger(TariffServiceImpl.class);

    @Override
    public ServiceCall<NotUsed, String> test() {
        log.info("TariffService is Up!!");
        return (request) -> completedFuture("TariffService is Up!!");
    }
}
