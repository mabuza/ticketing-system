package system.ticketing.profile.model;

import lombok.Data;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.search.annotations.*;
import org.hibernate.search.annotations.Parameter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by Ncube on 2/5/17.
 */
@Entity
@NamedQueries({
        @NamedQuery(name="getByEntityNameAndInstanceName", query="SELECT a FROM AuditTrail a WHERE a.entityName = :entityName AND a.instanceName = :instanceName ORDER BY a.time DESC"),
        @NamedQuery(name="getByEntityNameAndEntityId", query="SELECT a FROM AuditTrail a WHERE a.entityName = :entityName AND a.entityId = :entityId ORDER BY a.time DESC"),
        @NamedQuery(name="getByEntityNameAndInstanceNameAndTimePeriod",query="SELECT a FROM AuditTrail a WHERE a.entityName = :entityName AND a.instanceName = :instanceName AND a.time BETWEEN :startTime AND :endTime ORDER BY a.time DESC"),
        @NamedQuery(name="getByUsername", query="SELECT a FROM AuditTrail a WHERE a.username = :username ORDER BY a.time DESC"),
        @NamedQuery(name="getById", query="SELECT a FROM AuditTrail a WHERE a.auditTrailId = :auditTrailId ORDER BY a.time DESC"),
        @NamedQuery(name="getByUsernameAndTimePeriod",query="SELECT a FROM AuditTrail a WHERE a.username = :username AND a.time BETWEEN :startTime AND :endTime ORDER BY a.time DESC"),
        @NamedQuery(name="getByActivityAndTimePeriod",query="SELECT a FROM AuditTrail a WHERE a.activity.id = :activityId AND a.time BETWEEN :startTime AND :endTime ORDER BY a.time DESC"),
        @NamedQuery(name="getByTimePeriod",query="SELECT a FROM AuditTrail a WHERE a.time BETWEEN :startTime AND :endTime ORDER BY a.time DESC")
})

@Table(name = "audittrail")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@AnalyzerDef(name = "auditanalyzer",
        tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
        filters = {
                @TokenFilterDef(factory = LowerCaseFilterFactory.class),
                @TokenFilterDef(factory = SnowballPorterFilterFactory.class, params = {
                        @Parameter(name = "language", value = "English")})})
@Data
@Indexed
public class AuditTrail implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(length=50)
    private String auditTrailId;
    @Column(length=100)
    @Field
    @Analyzer(definition = "auditanalyzer")
    private String instanceName;
    @Column(length=50)
    @Field
    @Analyzer(definition = "auditanalyzer")
    private String entityName;
    @Column(length=50)
    @Field
    @Analyzer(definition = "auditanalyzer")
    private String entityId;
    private LocalDateTime time;
    @Column(length=2000)
    private String narrative;
    @Column(length=50)
    @Field
    @Analyzer(definition = "auditanalyzer")
    private String username;
    @ManyToOne
    private Activity activity;

    public AuditTrail() {
        super();
    }
    public String getAuditTrailId() {
        return this.auditTrailId;
    }

    public void setAuditTrailId(String auditTrailId) {
        this.auditTrailId = auditTrailId;
    }

    public Activity getActivity() {
        return activity;
    }
    public void setActivity(Activity activity) {
        this.activity = activity;
    }
    public LocalDateTime getTime() {
        return this.time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
    public String getNarrative() {
        return this.narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEntityName() {
        return entityName;
    }
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
    public String getEntityId() {
        return entityId;
    }
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
    public String getInstanceName() {
        return instanceName;
    }
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
}
