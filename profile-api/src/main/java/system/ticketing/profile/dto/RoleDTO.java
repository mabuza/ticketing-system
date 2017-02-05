package system.ticketing.profile.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Data;
import system.ticketing.utilities.enums.EntityStatus;
import system.ticketing.utilities.util.GenerateKey;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Created by Ncube on 2/5/17.
 */
@Data
public class RoleDTO implements Jsonable{
    private String id;
    private String name;
    private boolean restricted;
    private String status;
    private LocalDateTime dateCreated;

    public RoleDTO(){}

    @JsonCreator
    public RoleDTO(
            @JsonProperty("id")String id,
            @JsonProperty("name") String name,
            @JsonProperty("restricted") boolean restricted,
            @JsonProperty("status") String status,
            @JsonProperty("dateCreated") LocalDateTime dateCreated){
        this.id = Optional.ofNullable(id).orElseGet(GenerateKey::generateEntityId);
        this.name = Preconditions.checkNotNull(name, "name");
        this.restricted = Optional.ofNullable(restricted).orElse(false);
        this.status = Optional.ofNullable(status).orElseGet(EntityStatus.PENDING_APPROVAL::name);
        this.dateCreated = Optional.ofNullable(dateCreated).orElseGet(LocalDateTime::now);
    }
}
