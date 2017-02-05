package system.ticketing.profile.model;

import lombok.Data;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.search.annotations.*;
import org.hibernate.search.annotations.Parameter;
import system.ticketing.utilities.audit.Auditable;
import system.ticketing.utilities.enums.EntityStatus;
import system.ticketing.utilities.enums.OwnerType;
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
        @NamedQuery(name="getAllProfiles", query="SELECT p FROM Profile p WHERE p.status <> :status"),
        @NamedQuery(name="getProfileByUserName", query="SELECT p FROM Profile p WHERE p.userName = :userName AND p.status <> :status"),
        @NamedQuery(name="getProfileByUserNameAndStatus", query="SELECT p FROM Profile p WHERE p.userName = :userName AND p.status = :status"),
        @NamedQuery(name="getProfileByUserRole", query="SELECT p FROM Profile p WHERE p.role.name LIKE:roleName AND p.status <> :status ORDER BY p.userName ASC"),
        @NamedQuery(name="getProfileByBranchId", query="SELECT p FROM Profile p WHERE p.branchId =:branchId AND p.status <> :status ORDER BY p.userName ASC"),
        @NamedQuery(name="getProfileByLastName", query="SELECT p FROM Profile p WHERE p.lastName LIKE:lastName AND p.status <> :status ORDER BY p.userName ASC"),
        @NamedQuery(name="getProfileByStatus", query="SELECT p FROM Profile p WHERE p.status = :status ORDER BY p.userName ASC"),
        @NamedQuery(name="getAllLoggedOnProfiles", query="SELECT p FROM Profile p WHERE p.loggedIn =:loggedIn  ORDER BY p.userName ASC"),
        @NamedQuery(name="getProfileByIP", query="SELECT p FROM Profile p WHERE p.ipAddress =:ipAddress ORDER BY p.userName ASC")
})
@Table(name = "profile")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@Indexed
@AnalyzerDef(name = "customanalyzer",
        tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
        filters = {
                @TokenFilterDef(factory = LowerCaseFilterFactory.class),
                @TokenFilterDef(factory = SnowballPorterFilterFactory.class, params = {
                        @Parameter(name = "language", value = "English")})})
public class Profile implements Auditable {
    @Id
    @Column(length=30)
    private String id;
    @Column(length=30)
    @Field
    @Analyzer(definition = "customanalyzer")
    private String lastName;
    @Column(length=30)
    @Field
    @Analyzer(definition = "customanalyzer")
    private String firstNames;
    @Column(length=30, unique = true)
    @Field
    @Analyzer(definition = "customanalyzer")
    private String userName;
    @Field
    @Analyzer(definition = "customanalyzer")
    @Column(length=30)
    private String ownerId;
    @Enumerated(EnumType.STRING)
    @Column(length=70)
    private OwnerType ownerType;
    @Enumerated(EnumType.STRING)
    @Column(length=70)
    private EntityStatus status;
    @Column(length=30)
    private String userPassword;
    @Column(length=30)
    @Field
    @Analyzer(definition = "customanalyzer")
    private String email;
    @Column(length=30)
    @Field
    @Analyzer(definition = "customanalyzer")
    private String phoneNumber;
    @Column(length=30)
    @Field
    @Analyzer(definition = "customanalyzer")
    private String mobileNumber;
    @Column
    private LocalDateTime lastLoginDate;
    @Column
    private int loginAttempts;
    @Column
    private LocalDateTime passwordExpiryDate;
    @Column
    private boolean changePassword;
    @Column
    private boolean loggedIn;
    @Column(length=30)
    private String ipAddress;
    @Column(length=30)
    private String branchId;
    @ManyToOne(targetEntity=Role.class, fetch=FetchType.EAGER)
    @IndexedEmbedded
    private Role role;
    @Column
    @Field(index = org.hibernate.search.annotations.Index.YES, analyze = Analyze.YES, store = Store.NO)
    @DateBridge(resolution = Resolution.MONTH)
    private LocalDateTime dateCreated;
    @Version
    @Column
    private long version;

    public void setFieldsToUpperCase() {
        this.setLastName(this.lastName.toUpperCase());
        this.setFirstNames(this.firstNames.toUpperCase());
    }

    @Override
    public Map<String, String> getAuditableAttributesMap() {
        Map<String, String> attributesMap = new HashMap<>();
        attributesMap.put("username", getUserName());
        attributesMap.put("firstName", getFirstNames());
        attributesMap.put("lastName", getLastName());
        attributesMap.put("userRole", getRole().getName());
        attributesMap.put("status", getStatus().toString());
        attributesMap.put("email", getEmail());
        attributesMap.put("ipAddress", getIpAddress());
        return attributesMap;
    }
    @Override
    public String getAuditableAttributesString() {
        Map<String,String> attributesMap = this.getAuditableAttributesMap();
        return MapUtil.convertAttributesMapToString(attributesMap);
    }
    @Override
    public String getEntityName() {
        return "PROFILE";
    }
    @Override
    public String getInstanceName() {
        return getUserName();
    }

    public String toString(){
        return getLastName()+" "+getFirstNames();
    }
}
