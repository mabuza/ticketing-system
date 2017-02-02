package system.ticketing.bookentry.impl;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import system.ticketing.bookentry.api.BookEntryService;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Created by Ncube on 2/2/17.
 */
public class BookEntryServiceImpl implements BookEntryService {
    private static final Logger log = LoggerFactory.getLogger(BookEntryServiceImpl.class);

    @Override
    public ServiceCall<NotUsed, String> test() {
        log.info("BookEntryService is Up!!");
        return (request) -> completedFuture("BookEntryService is Up!!");
    }
}
