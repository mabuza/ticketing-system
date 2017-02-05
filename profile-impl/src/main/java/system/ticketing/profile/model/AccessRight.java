package system.ticketing.profile.model;

import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.search.annotations.*;
import system.ticketing.utilities.enums.AccessRightStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by Ncube on 2/5/17.
 */
@Entity
@NamedQueries({
        @NamedQuery(name="getAccessRight", query="SELECT a FROM AccessRight a WHERE NOT a.status =:status ORDER BY a.actionName ASC"),
        @NamedQuery(name="getAccessRightByActionName", query="SELECT a FROM AccessRight a WHERE a.actionName LIKE:actionName"),
        @NamedQuery(name="getAccessRightByActualActionName", query="SELECT a FROM AccessRight a WHERE a.actionName = :actionName")
})
@Table(name = "accessright")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@Indexed
public class AccessRight {
    @Id
    @Column(length=30)
    private String id;
    @Column(length=50, unique = true)
    @Field(index = org.hibernate.search.annotations.Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String actionName;
    @Enumerated(EnumType.STRING)
    private AccessRightStatus status;
    @OneToMany(targetEntity=RoleAccessRight.class, mappedBy="accessRight", fetch = FetchType.LAZY)
    private List<RoleAccessRight> roleAccessRights;
    @Column
    @Field(index = org.hibernate.search.annotations.Index.YES, analyze = Analyze.YES, store = Store.NO)
    @DateBridge(resolution = Resolution.DAY)
    private LocalDateTime dateCreated;
    @Version
    @Column
    private long version;

    public void setActionName(String actionName) {
        this.actionName = actionName.toUpperCase();
    }

    public void setFieldsToUpperCase() {
        this.setActionName(this.actionName.toUpperCase());
    }

}
