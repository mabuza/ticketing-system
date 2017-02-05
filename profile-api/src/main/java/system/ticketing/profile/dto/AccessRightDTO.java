package system.ticketing.profile.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Data;
import system.ticketing.utilities.enums.AccessRightStatus;
import system.ticketing.utilities.util.GenerateKey;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Created by Ncube on 2/5/17.
 */
@Data
public class AccessRightDTO implements Jsonable{
    private String id;
    private String actionName;
    private String status;
    private LocalDateTime dateCreated;

    public AccessRightDTO(){}

    @JsonCreator
    public AccessRightDTO(@JsonProperty("id") String id,
                          @JsonProperty("actionName") String actionName,
                          @JsonProperty("status") String status,
                          @JsonProperty("dateCreated") LocalDateTime dateCreated){
        this.id = Optional.ofNullable(id).orElseGet(GenerateKey::generateEntityId);
        this.actionName = actionName;
        this.status = Optional.ofNullable(status).orElseGet(AccessRightStatus.ENABLED::name);
        this.dateCreated = Optional.ofNullable(dateCreated).orElseGet(LocalDateTime::now);
    }
}
