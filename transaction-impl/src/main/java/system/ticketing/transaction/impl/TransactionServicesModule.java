package system.ticketing.transaction.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import system.ticketing.transaction.api.TransactionService;

/**
 * Created by Ncube on 2/2/17.
 */
public class TransactionServicesModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindServices(
                serviceBinding(TransactionService.class, TransactionServiceImpl.class)
        );
    }
}
