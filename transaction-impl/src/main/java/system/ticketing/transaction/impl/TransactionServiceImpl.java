package system.ticketing.transaction.impl;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import system.ticketing.transaction.api.TransactionService;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Created by Ncube on 2/2/17.
 */
public class TransactionServiceImpl implements TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Override
    public ServiceCall<NotUsed, String> test() {
        log.info("TransactionService is Up!!");
        return (request) -> completedFuture("TransactionService is Up!!");
    }
}
