package system.ticketing.profile.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Data;

import java.util.Optional;

/**
 * Created by Ncube on 2/5/17.
 */
@Data
@Builder
public class LogDTO {
    private String userName;
    private String activityName;
    private String entityId;
    private String entityName;
    private String oldObject;
    private String newObject;
    private String instanceName;
    private String narrative;

    public LogDTO(){}

    @JsonCreator
    public LogDTO(String userName, String activityName, String entityId, String entityName, String oldObject,
                  String newObject, String instanceName, String narrative){
        this.userName = Preconditions.checkNotNull(userName, "userName");
        this.activityName = Preconditions.checkNotNull(activityName, "activityName");
        this.entityId = Optional.ofNullable(entityId).orElse(null);
        this.entityName = Optional.ofNullable(entityName).orElse(null);
        this.oldObject = Optional.ofNullable(oldObject).orElse(null);
        this.newObject = Optional.ofNullable(newObject).orElse(null);
        this.instanceName = Optional.ofNullable(instanceName).orElse(null);
        this.narrative = Optional.ofNullable(narrative).orElse(null);
    }
}
