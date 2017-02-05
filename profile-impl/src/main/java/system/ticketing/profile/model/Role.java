package system.ticketing.profile.model;

import lombok.Data;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.search.annotations.*;
import system.ticketing.utilities.audit.Auditable;
import system.ticketing.utilities.enums.EntityStatus;
import system.ticketing.utilities.util.MapUtil;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ncube on 2/5/17.
 */
@Entity
@NamedQueries({
        @NamedQuery(name="getAllRoles",query="SELECT r FROM Role r ORDER BY r.name ASC"),
        @NamedQuery(name="getActiveRoles",query="SELECT r FROM Role r WHERE r.status <> :status "),
        @NamedQuery(name="getRoleByRoleName",query="SELECT r FROM Role r WHERE r.name =:name"),
        @NamedQuery(name="getRoleByStatus",query="SELECT r FROM Role r WHERE r.status =:status ORDER BY r.name ASC")
})
@Table(name = "role")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@AnalyzerDef(name = "roleanalyzer",
        tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
        filters = {
                @TokenFilterDef(factory = LowerCaseFilterFactory.class),
                @TokenFilterDef(factory = SnowballPorterFilterFactory.class, params = {
                        @org.hibernate.search.annotations.Parameter(name = "language", value = "English")})})
@Data
@Indexed
public class Role implements Serializable, Auditable {
    @Id
    @Column(length=30)
    private String id;
    @Column(length=30, unique = true)
    @Field
    @Analyzer(definition = "roleanalyzer")
    private String name;
    @Column
    private boolean restricted;
    @Enumerated(EnumType.STRING)
    private EntityStatus status;
    @OneToMany(targetEntity=RoleAccessRight.class, mappedBy = "role", fetch = FetchType.LAZY)
    private List<RoleAccessRight> roleAccessRights;
    @OneToMany(targetEntity=Profile.class, mappedBy = "role", fetch = FetchType.LAZY)
    private List<Profile> profiles;
    @Column
    private LocalDateTime dateCreated;
    @Version
    @Column
    private int version;

    private static final long serialVersionUID = 1L;

    public Role() {
        super();
    }

    @Override
    public Map<String, String> getAuditableAttributesMap() {
        Map<String, String> attributesMap = new HashMap<>();
        attributesMap.put("name", getName());
        attributesMap.put("status", getStatus().toString());
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
        return getName();
    }

}
