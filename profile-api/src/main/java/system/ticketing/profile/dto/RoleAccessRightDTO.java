package system.ticketing.profile.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Data;
import system.ticketing.utilities.util.GenerateKey;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Created by Ncube on 2/5/17.
 */
@Data
public class RoleAccessRightDTO implements Jsonable{
    private String id;
    private boolean canDo;
    private RoleDTO role;
    private String roleId;
    private String roleName;
    private String accessRightId;
    private String actionName;
    private AccessRightDTO accessRight;
    private LocalDateTime dateCreated;

    public RoleAccessRightDTO(){}

    @JsonCreator
    public RoleAccessRightDTO(
            @JsonProperty("id") String id,
            @JsonProperty("canDo") boolean canDo,
            @JsonProperty("role") RoleDTO role,
            @JsonProperty("roleId") String roleId,
            @JsonProperty("roleName") String roleName,
            @JsonProperty("accessRightId") String accessRightId,
            @JsonProperty("actionName") String actionName,
            @JsonProperty("accessRight") AccessRightDTO accessRight,
            @JsonProperty("dateCreated") LocalDateTime dateCreated){
        this.id = Optional.ofNullable(id).orElseGet(GenerateKey::generateEntityId);
        this.canDo = Preconditions.checkNotNull(canDo, "canDo");
        this.role = role;
        this.roleId = Optional.ofNullable(roleId).orElse(role.getId());
        this.roleName = roleName;
        this.accessRightId = Optional.ofNullable(accessRightId).orElse(accessRight.getId());
        this.actionName = actionName;
        this.accessRight = accessRight;
        this.dateCreated = Optional.ofNullable(dateCreated).orElseGet(LocalDateTime::now);
    }
}
