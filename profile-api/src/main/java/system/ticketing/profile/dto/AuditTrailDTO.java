package system.ticketing.profile.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Data;
import system.ticketing.utilities.util.GenerateKey;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Created by Ncube on 2/5/17.
 */
@Data
public class AuditTrailDTO implements Jsonable{
    private String auditTrailId;
    private String instanceName;
    private String entityName;
    private String entityId;
    private LocalDateTime time;
    private String narrative;
    private String userName;
    private String activityId;
    private String activityName;

    public AuditTrailDTO(){}

    @JsonCreator
    public AuditTrailDTO(@JsonProperty("auditTrailId") String auditTrailId,
                         @JsonProperty("instanceName") String instanceName,
                         @JsonProperty("entityName") String entityName,
                         @JsonProperty("entityId") String entityId,
                         @JsonProperty("time") LocalDateTime time,
                         @JsonProperty("narrative") String narrative,
                         @JsonProperty("userName") String userName,
                         @JsonProperty("activityId") String activityId,
                         @JsonProperty("activityName") String activityName){
        this.auditTrailId = Optional.ofNullable(auditTrailId).orElseGet(GenerateKey::generateEntityId);
        this.instanceName = instanceName;
        this.entityName = entityName;
        this.entityId = entityId;
        this.time = Optional.ofNullable(time).orElseGet(LocalDateTime::now);
        this.narrative = narrative;
        this.userName = userName;
        this.activityId = activityId;
        this.activityName = activityName;
    }
}
