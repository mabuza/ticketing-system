package system.ticketing.processmodule.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import system.ticketing.processmodule.api.ProcessModuleService;

/**
 * Created by Ncube on 2/2/17.
 */
public class ProcessModuleServicesModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindServices(
                serviceBinding(ProcessModuleService.class, ProcessModuleServiceImpl.class)
        );
    }
}
