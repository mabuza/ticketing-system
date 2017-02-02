package system.ticketing.profile.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import static com.lightbend.lagom.javadsl.api.Service.*;

/**
 * Created by Ncube on 2/2/17.
 */
public interface ProfileService extends Service{
    ServiceCall<NotUsed, String> test();

    @Override
    default Descriptor descriptor() {
        // @formatter:off
        return named("profileService").withCalls(
                restCall(Method.GET, "/api/profile/test", this::test)
        ).withAutoAcl(true);
        // @formatter:on
    }
}
