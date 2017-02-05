package system.ticketing.profile.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.api.ConfigurationServiceLocator;
import com.lightbend.lagom.javadsl.api.ServiceLocator;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import play.Configuration;
import play.Environment;
import system.ticketing.profile.api.AuditService;
import system.ticketing.profile.api.ProfileService;
import system.ticketing.profile.api.RoleService;

/**
 * Created by Ncube on 2/2/17.
 */
public class ProfileServicesModule extends AbstractModule implements ServiceGuiceSupport {
    private final Environment environment;
    private final Configuration configuration;

    public ProfileServicesModule(Environment environment, Configuration configuration) {
        this.environment = environment;
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        if (environment.isProd()) {
            bind(ServiceLocator.class).to(ConfigurationServiceLocator.class);
        }
        bindServices(
                serviceBinding(ProfileService.class, ProfileServiceImpl.class),
                serviceBinding(RoleService.class, RoleServiceImpl.class),
                serviceBinding(AuditService.class, AuditServiceImpl.class)
        );

    }
}
