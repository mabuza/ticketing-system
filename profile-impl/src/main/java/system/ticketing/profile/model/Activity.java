package system.ticketing.profile.model;

import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.search.annotations.*;
import system.ticketing.utilities.audit.Auditable;
import system.ticketing.utilities.util.MapUtil;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ncube on 2/5/17.
 */
@Entity
@NamedQueries({
        @NamedQuery(name="getActivity",query="SELECT a FROM Activity a"),
        @NamedQuery(name="getActivityById",query = "SELECT a FROM Activity a WHERE a.id =:id"),
        @NamedQuery(name="getActivityByName",query = "SELECT a FROM Activity a WHERE a.name = :name ORDER BY a.name ASC"),
        @NamedQuery(name="getActivityByLogged",query = "SELECT a FROM Activity a WHERE a.logged = :logged")
})

@Table(name = "activity")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@Indexed
public class Activity implements Serializable, Auditable {
    @Id
    @Column(length=50)
    private String id;
    @Column(length=60)
    @Field(index = org.hibernate.search.annotations.Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String name;
    @Column
    private boolean logged;
    @OneToMany(mappedBy="activity")
    private List<AuditTrail> auditTrails;
    @Version
    private long version;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean isLogged() {
        return logged;
    }
    public void setLogged(boolean logged) {
        this.logged = logged;
    }
    public long getVersion() {
        return version;
    }
    public void setVersion(long version) {
        this.version = version;
    }
    public List<AuditTrail> getAuditTrails() {
        return auditTrails;
    }
    public void setAuditTrails(List<AuditTrail> auditTrails) {
        this.auditTrails = auditTrails;
    }
    @Override
    public Map<String, String> getAuditableAttributesMap() {
        Map<String, String> attributesMap = new HashMap<String, String>();
        attributesMap.put("logged", isLogged()+"");
        return attributesMap;
    }
    @Override
    public String getAuditableAttributesString() {
        Map<String,String> attributesMap = this.getAuditableAttributesMap();
        return MapUtil.convertAttributesMapToString(attributesMap);
    }
    @Override
    public String getEntityName() {
        return "ACTIVITY";
    }
    @Override
    public String getInstanceName() {
        return this.getName();
    }
    @Override
    public String toString() {
        return this.getName();
    }
}
