package system.ticketing.tariff.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import system.ticketing.tariff.api.TariffService;

/**
 * Created by Ncube on 2/2/17.
 */
public class TariffServicesModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindServices(
                serviceBinding(TariffService.class, TariffServiceImpl.class)
        );
    }
}
