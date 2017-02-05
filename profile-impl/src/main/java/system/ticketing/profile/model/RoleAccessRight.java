package system.ticketing.profile.model;

import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import system.ticketing.utilities.audit.Auditable;
import system.ticketing.utilities.util.MapUtil;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ncube on 2/5/17.
 */
@Entity
@NamedQueries({
        @NamedQuery(name="getRoleAccessRightByRole", query="SELECT p FROM RoleAccessRight p WHERE p.role.id =:roleId ORDER BY p.accessRight.actionName ASC"),
        @NamedQuery(name = "getRoleAccessRightByRoleAndActionName", query = "SELECT p FROM RoleAccessRight p WHERE p.role.id =:roleId AND p.accessRight.actionName =:actionName"),
        @NamedQuery(name="getRoleAccessRightByRoleNameAndActionName", query="SELECT p FROM RoleAccessRight p WHERE p.role.name =:roleName " +
                "AND p.accessRight.actionName = :actionName")
})
@Table(name = "roleaccessright")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
public class RoleAccessRight implements Auditable{
    @Id
    @Column(length=30)
    private String id;
    @Column
    private boolean canDo;
    @ManyToOne(targetEntity = Role.class)
    private Role role;
    @ManyToOne(targetEntity = AccessRight.class)
    private AccessRight accessRight;
    @Column
    private LocalDateTime dateCreated;
    @Version
    @Column
    private long version;

    @Override
    public Map<String, String> getAuditableAttributesMap() {
        Map<String, String> attributesMap = new HashMap<>();
        attributesMap.put("canDo", canDo+"");
        return attributesMap;
    }
    @Override
    public String getAuditableAttributesString() {
        return MapUtil.convertAttributesMapToString(getAuditableAttributesMap());
    }
    @Override
    public String getEntityName() {
        return "ROLE";
    }
    @Override
    public String getInstanceName() {
        return role.getName();
    }
}
