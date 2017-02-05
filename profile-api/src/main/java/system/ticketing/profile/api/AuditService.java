package system.ticketing.profile.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import system.ticketing.profile.dto.ActivityDTO;
import system.ticketing.profile.dto.AuditTrailDTO;
import system.ticketing.profile.dto.LogDTO;

import java.util.List;

import static com.lightbend.lagom.javadsl.api.Service.*;

/**
 * Created by Ncube on 2/5/17.
 */
public interface AuditService extends Service {
    ServiceCall<NotUsed, String> test();
    ServiceCall<LogDTO, AuditTrailDTO> logActivity();
    ServiceCall<LogDTO, AuditTrailDTO> logActivityWithNarrative();
    ServiceCall<NotUsed, AuditTrailDTO> getById(String id);
    ServiceCall<NotUsed, List<AuditTrailDTO>> getByEntityNameAndInstanceName(String entityName, String instanceName);
    ServiceCall<NotUsed, List<AuditTrailDTO>> getByEntityNameAndInstanceNameAndTimePeriod(
            String entityName, String instanceName, String startTime, String endTime);
    ServiceCall<NotUsed, List<AuditTrailDTO>> getByUsername(String userName);
    ServiceCall<NotUsed, List<AuditTrailDTO>> getByUsernameAndTimePeriod(
            String userName, String startTime, String endTime);
    ServiceCall<NotUsed, List<AuditTrailDTO>> getByActivityAndTimePeriod(
            String activityId, String startTime, String endTime);
    ServiceCall<NotUsed, List<AuditTrailDTO>> getByEntityNameAndEntityId(String entityName, String entityId);
    ServiceCall<NotUsed, List<AuditTrailDTO>> getByTimePeriod(String startTime, String endTime);
    ServiceCall<NotUsed, ActivityDTO> getActivityByName(String name);
    ServiceCall<NotUsed, List<ActivityDTO>> getAllActivities();
    ServiceCall<NotUsed, ActivityDTO> findActivityById(String id);
    ServiceCall<ActivityDTO, ActivityDTO> editActivity();
    ServiceCall<NotUsed, List<AuditTrailDTO>> search(String search);

    @Override
    default Descriptor descriptor() {
        // @formatter:off
        return named("auditService").withCalls(
                restCall(Method.GET, "/api/audit/test", this::test),
                restCall(Method.POST, "/api/audits", this::logActivity),
                restCall(Method.POST, "/api/audits/narrative", this::logActivityWithNarrative),
                restCall(Method.GET, "/api/audits/:id", this::getById),
                restCall(Method.GET, "/api/audits/?search", this::search),
                restCall(Method.GET, "/api/audits/instance/:entityName/:instanceName", this::getByEntityNameAndInstanceName),
                restCall(Method.GET, "/api/audits/:entityName/:instanceName/:startTime/:endTime", this::getByEntityNameAndInstanceNameAndTimePeriod),
                restCall(Method.GET, "/api/audits/?userName", this::getByUsername),
                restCall(Method.GET, "/api/audits/:userName/:startTime/:endTime", this::getByUsernameAndTimePeriod),
                restCall(Method.GET, "/api/audits/id/:activityId/:startTime/:endTime", this::getByActivityAndTimePeriod),
                restCall(Method.GET, "/api/audits/:entityName/:entityId", this::getByEntityNameAndEntityId),
                restCall(Method.GET, "/api/audits/time/:startTime/:endTime", this::getByTimePeriod),

                restCall(Method.GET, "/api/activities/name/:name", this::getActivityByName),
                restCall(Method.GET, "/api/activities", this::getAllActivities),
                restCall(Method.GET, "/api/activities/:id", this::findActivityById),
                restCall(Method.PUT, "/api/activities", this::editActivity)
        ).withAutoAcl(true);
        // @formatter:on
    }
}
