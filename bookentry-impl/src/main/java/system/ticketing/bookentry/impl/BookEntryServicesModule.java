package system.ticketing.bookentry.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import system.ticketing.bookentry.api.BookEntryService;

/**
 * Created by Ncube on 2/2/17.
 */
public class BookEntryServicesModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindServices(
                serviceBinding(BookEntryService.class, BookEntryServiceImpl.class)
        );
    }
}
