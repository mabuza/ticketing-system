package system.ticketing.profile.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import system.ticketing.profile.api.ProfileService;

/**
 * Created by Ncube on 2/2/17.
 */
public class ProfileServicesModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindServices(
                serviceBinding(ProfileService.class, ProfileServiceImpl.class)
        );
    }
}
