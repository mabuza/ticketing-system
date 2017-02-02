package system.ticketing.registration.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import system.ticketing.registration.api.RegistrationService;

/**
 * Created by Ncube on 2/2/17.
 */
public class RegistrationServicesModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindServices(
                serviceBinding(RegistrationService.class, RegistrationServiceImpl.class)
        );
    }
}
