package system.ticketing.profile.impl;

import akka.NotUsed;
import com.google.inject.Inject;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.JPAApi;
import system.ticketing.profile.api.AuditService;
import system.ticketing.profile.dto.ActivityDTO;
import system.ticketing.profile.dto.AuditTrailDTO;
import system.ticketing.profile.dto.LogDTO;
import system.ticketing.profile.mapper.DTOMapper;
import system.ticketing.profile.model.Activity;
import system.ticketing.profile.model.AuditTrail;
import system.ticketing.profile.util.AuditEvents;
import system.ticketing.utilities.util.GenerateKey;
import system.ticketing.utilities.util.MapUtil;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Created by Ncube on 2/5/17.
 */
@SuppressWarnings("ALL")
public class AuditServiceImpl implements AuditService {
    static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);

    private JPAApi jpaApi;

    @Inject
    public AuditServiceImpl(JPAApi jpaApi){
        this.jpaApi = jpaApi;
    }

    @Override
    public ServiceCall<NotUsed, String> test() {
        return (request) -> completedFuture("AuditService is Up!!");
    }

    @Override
    public ServiceCall<LogDTO, AuditTrailDTO> logActivity() {

        return (request) -> {
            AuditTrail auditTrail = null;
            final String name = request.getActivityName();
            Activity activity = createActivity(name);

            if(activity.isLogged()){
                log.info("Activity is logged : {}", activity.getName());
                if(AuditEvents.RESET_MOBILE_PROFILE_PIN.equals(activity.getName())){

                    auditTrail = logPinReset(request.getEntityId(), request.getEntityName(), activity, request.getUserName(), request.getInstanceName(), request.getOldObject());
                }else{
                    try {
                        auditTrail = logChanges(request.getNewObject(), request.getOldObject(), request.getEntityId(), request.getEntityName(),	activity, request.getUserName(), request.getInstanceName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            log.info("Audit Trail : {}", auditTrail);
            return completedFuture(DTOMapper.toAuditTrailDTO(auditTrail));
        };
    }

    @Override
    public ServiceCall<LogDTO, AuditTrailDTO> logActivityWithNarrative() {
        return (request) -> {
            AuditTrail auditTrail = null;
            final String name = request.getActivityName();
            Activity activity = createActivity(name);

            auditTrail = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                AuditTrail _auditTrail = null;
                if(activity.isLogged()){
                    try {
                        _auditTrail = new AuditTrail();
                        _auditTrail.setAuditTrailId(GenerateKey.generateEntityId());
                        _auditTrail.setUsername(request.getUserName().toUpperCase());
                        _auditTrail.setActivity(activity);
                        _auditTrail.setTime(LocalDateTime.now());
                        _auditTrail.setNarrative(request.getNarrative().toUpperCase());
                        em.persist(_auditTrail);
                    }catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
                return _auditTrail;

            });

            log.info("Audit Trail : {}", auditTrail);
            return completedFuture(DTOMapper.toAuditTrailDTO(auditTrail));
        };
    }

    @Override
    public ServiceCall<NotUsed, AuditTrailDTO> getById(String id) {
        return (request) -> {
            AuditTrail auditTrail = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getById");
                query.setParameter("auditTrailId", id);
                return (AuditTrail) query.getSingleResult();
            });
            return completedFuture(DTOMapper.toAuditTrailDTO(auditTrail));
        };
    }

    private Activity createActivity(String activityName) {
        Activity activity = jpaApi.withTransaction(() -> {
            EntityManager em = jpaApi.em();
            Activity _activity = this.getActivityByNamePrivate(activityName);
            log.info("Retrieved Activity = " + _activity + ", Activity Name = " + activityName);
            if(_activity == null){
                _activity = new Activity();
                _activity.setId(GenerateKey.generateEntityId());
                _activity.setName(activityName);
                _activity.setLogged(true);
                em.persist(_activity);
            }
            return _activity;
        });
        return  activity;
    }

    @Override
    public ServiceCall<NotUsed, List<AuditTrailDTO>> getByEntityNameAndInstanceName(String entityName, String instanceName) {
        return (request) -> {

            List<AuditTrail> auditTrails = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getByEntityNameAndInstanceName");
                query.setParameter("entityName", entityName);
                query.setParameter("instanceName", instanceName);
                return (List<AuditTrail>) query.getResultList();
            });

            return completedFuture(Collections.unmodifiableList(
                    auditTrails.parallelStream()
                            .map(auditTrail -> DTOMapper.toAuditTrailDTO(auditTrail))
                            .collect(Collectors.toList()))
            );
        };
    }

    @Override
    public ServiceCall<NotUsed, List<AuditTrailDTO>> getByEntityNameAndInstanceNameAndTimePeriod(String entityName,
                                                                                                 String instanceName,
                                                                                                 String startTime,
                                                                                                 String endTime) {
        return (request) -> {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime startDateTime = LocalDateTime.parse(startTime, formatter);
            LocalDateTime endDateTime = LocalDateTime.parse(endTime, formatter);

            List<AuditTrail> auditTrails = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getByEntityNameAndInstanceNameAndTimePeriod");
                query.setParameter("entityName", entityName);
                query.setParameter("instanceName", instanceName);
                query.setParameter("startTime", this.setMinTime(startDateTime));
                query.setParameter("endTime", this.setMaxTime(endDateTime));
                return (List<AuditTrail>) query.getResultList();
            });

            return completedFuture(Collections.unmodifiableList(
                    auditTrails.parallelStream()
                            .map(DTOMapper::toAuditTrailDTO)
                            .collect(Collectors.toList()))
            );
        };
    }

    @Override
    public ServiceCall<NotUsed, List<AuditTrailDTO>> getByUsername(String userName) {
        return (request) -> {

            List<AuditTrail> auditTrails = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getByUsername");
                query.setParameter("username", userName);
                return (List<AuditTrail>) query.getResultList();
            });

            return completedFuture(Collections.unmodifiableList(
                    auditTrails.parallelStream()
                            .map(DTOMapper::toAuditTrailDTO)
                            .collect(Collectors.toList()))
            );
        };
    }

    @Override
    public ServiceCall<NotUsed, List<AuditTrailDTO>> getByUsernameAndTimePeriod(String userName, String startTime, String endTime) {
        return (request) -> {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime startDateTime = LocalDateTime.parse(startTime, formatter);
            LocalDateTime endDateTime = LocalDateTime.parse(endTime, formatter);

            List<AuditTrail> auditTrails = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getByUsernameAndTimePeriod");
                query.setParameter("username", userName);
                query.setParameter("startTime", this.setMinTime(startDateTime));
                query.setParameter("endTime", this.setMaxTime(endDateTime));
                return (List<AuditTrail>) query.getResultList();
            });

            return completedFuture(Collections.unmodifiableList(
                    auditTrails.parallelStream()
                            .map(DTOMapper::toAuditTrailDTO)
                            .collect(Collectors.toList()))
            );
        };
    }

    @Override
    public ServiceCall<NotUsed, List<AuditTrailDTO>> getByActivityAndTimePeriod(String activityId, String startTime, String endTime) {
        return (request) -> {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime startDateTime = LocalDateTime.parse(startTime, formatter);
            LocalDateTime endDateTime = LocalDateTime.parse(endTime, formatter);

            List<AuditTrail> auditTrails = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getByActivityAndTimePeriod");
                query.setParameter("activityId", activityId);
                query.setParameter("startTime", this.setMinTime(startDateTime));
                query.setParameter("endTime", this.setMaxTime(endDateTime));
                return (List<AuditTrail>) query.getResultList();
            });

            return completedFuture(Collections.unmodifiableList(
                    auditTrails.parallelStream()
                            .map(DTOMapper::toAuditTrailDTO)
                            .collect(Collectors.toList()))
            );
        };
    }

    @Override
    public ServiceCall<NotUsed, List<AuditTrailDTO>> getByEntityNameAndEntityId(String entityName, String entityId) {
        return (request) -> {
            List<AuditTrail> auditTrails = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getByEntityNameAndEntityId");
                query.setParameter("entityName", entityName);
                query.setParameter("entityId", entityId);
                return (List<AuditTrail>) query.getResultList();
            });

            return completedFuture(Collections.unmodifiableList(
                    auditTrails.parallelStream()
                            .map(DTOMapper::toAuditTrailDTO)
                            .collect(Collectors.toList()))
            );
        };
    }

    @Override
    public ServiceCall<NotUsed, List<AuditTrailDTO>> getByTimePeriod(String startTime, String endTime) {
        return (request) -> {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime startDateTime = LocalDateTime.parse(startTime, formatter);
            LocalDateTime endDateTime = LocalDateTime.parse(endTime, formatter);
            List<AuditTrail> auditTrails = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getByTimePeriod");
                query.setParameter("startTime", this.setMinTime(startDateTime));
                query.setParameter("endTime", this.setMaxTime(endDateTime));
                return (List<AuditTrail>) query.getResultList();
            });

            return completedFuture(Collections.unmodifiableList(
                    auditTrails.parallelStream()
                            .map(DTOMapper::toAuditTrailDTO)
                            .collect(Collectors.toList()))
            );
        };
    }

    @Override
    public ServiceCall<NotUsed, ActivityDTO> getActivityByName(String name) {
        return (request) -> {
            Activity activity = this.getActivityByNamePrivate(name);
            return completedFuture(DTOMapper.toActivityDTO(activity));
        };
    }

    private Activity getActivityByNamePrivate(String name){
        Activity activity = jpaApi.withTransaction(() -> {
            Session session = (Session) jpaApi.em().getDelegate();
            org.hibernate.Query query = session.getNamedQuery("getActivityByName");
            query.setCacheable(true);
            query.setParameter("name", name);
            return (Activity) query.uniqueResult();
        });

        return activity;
    }

    @Override
    public ServiceCall<NotUsed, List<ActivityDTO>> getAllActivities() {
        return (request) -> {
            List<Activity> activities = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getActivity");
                return (List<Activity>) query.getResultList();
            });

            return completedFuture(Collections.unmodifiableList(
                    activities.parallelStream()
                            .map(DTOMapper::toActivityDTO)
                            .collect(Collectors.toList()))
            );
        };
    }

    @Override
    public ServiceCall<NotUsed, ActivityDTO> findActivityById(String id) {
        return (request) -> {
            Activity activity = this.findActivityByIdPrivate(id);
            return completedFuture(DTOMapper.toActivityDTO(activity));
        };
    }

    private Activity findActivityByIdPrivate(String id){
        Activity activity = jpaApi.withTransaction(() -> {
            Session session = (Session) jpaApi.em().getDelegate();
            return session.load(Activity.class, id);
        });
        return activity;
    }

    @Override
    public ServiceCall<ActivityDTO, ActivityDTO> editActivity() {
        return (request) -> {
            jpaApi.withTransaction(() -> {
                //String oldActivity = this.findActivityByIdPrivate(request.getId()).getAuditableAttributesString();
                Activity newEntity = DTOMapper.toActivityEntity(request);
                newEntity.setName(AuditEvents.EDIT_ACTIVITY);

                jpaApi.em().persist(newEntity);
            });
            this.logActivity();

            return completedFuture(request);
        };
    }

    @Override
    public ServiceCall<NotUsed, List<AuditTrailDTO>> search(String search) {
        return (request) -> {
            log.info("Searching...");
            List<AuditTrail> results = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Session session = em.unwrap(Session.class);
                FullTextSession fullTextSession = Search.getFullTextSession(session);

                /*try {
                    fullTextSession.createIndexer().startAndWait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/

                QueryBuilder queryBuilder = fullTextSession.getSearchFactory()
                        .buildQueryBuilder().forEntity(AuditTrail.class).get();
                org.apache.lucene.search.Query query = queryBuilder
                        .keyword()
                        .fuzzy()
                        .onFields("auditTrailId", "instanceName", "entityName", "entityId", "username")
                        .matching(search)
                        .createQuery();
                org.hibernate.Query hibQuery = fullTextSession.createFullTextQuery(query, AuditTrail.class);
                List<AuditTrail> result = hibQuery.list();
                return result;
            });
            return completedFuture(
                    Collections.unmodifiableList(
                            results.parallelStream().map(auditTrail -> DTOMapper.toAuditTrailDTO(auditTrail))
                                    .collect(Collectors.toList())));
        };
    }

    private LocalDateTime setMinTime(LocalDateTime date){
        if(date == null){
            return null;
        }
        return date.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    private LocalDateTime setMaxTime(LocalDateTime date){
        if(date == null){
            return null;
        }
        return date.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    private AuditTrail logChanges(String newObject, String oldObject,
                                  String entityId, String entityName, Activity activity, String username,
                                  String instanceName) throws Exception {
        //Set the common properties for an audit trail object

        AuditTrail auditTrail;
        try {
            auditTrail = new AuditTrail();
            auditTrail.setAuditTrailId(GenerateKey.generateEntityId());
            auditTrail.setUsername(username.toUpperCase());
            auditTrail.setEntityName(entityName);
            auditTrail.setActivity(activity);
            auditTrail.setEntityName(entityName);
            auditTrail.setInstanceName(instanceName);
            auditTrail.setTime(LocalDateTime.now());
            auditTrail.setEntityId(entityId);
            if (oldObject == null) {
                //If its a case of object creation
                Map<String, String> newObjectMap = MapUtil
                        .convertAttributesStringToMap(newObject);
                String narrative = "";
                for (Object fieldName : newObjectMap.keySet()) {
                    narrative=narrative+fieldName.toString() + " set to "+ newObjectMap.get(fieldName).toString() + ", ";
                }

                //System.out.println("##########"+narrative+"##########");
                auditTrail.setNarrative(narrative.toUpperCase());
            } else {
                //In the case of object update
                Map<String, String> newObjectMap = MapUtil
                        .convertAttributesStringToMap(newObject);
                Map<String, String> oldObjectMap = MapUtil
                        .convertAttributesStringToMap(oldObject);
                String narrative = "";
                for (Object fieldName : newObjectMap.keySet()) {
                    String oldPropertyValue = oldObjectMap.get(fieldName);
                    String newPropertyValue = newObjectMap.get(fieldName);
                    if (!oldPropertyValue.equalsIgnoreCase(newPropertyValue)) {
                        narrative=narrative+fieldName.toString() + " changed from "
                                + oldPropertyValue + " to " + newPropertyValue
                                + ", ";
                    }
                }
                log.debug("1 ##########"+oldObject+"##########");
                log.debug("2 ##########"+newObject+"##########");
                log.debug("3 ########## Narrative = "+narrative.length()+"##########");
                log.debug("1 ########## Entity Name = "+entityName.length()+"##########");
                log.debug("1 ########## User Name = "+username.length()+"##########");
                log.debug("1 ##########"+oldObject+"##########");
                auditTrail.setNarrative(narrative.toUpperCase());
            }
            jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                em.persist(auditTrail);
            });
            return auditTrail;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    private AuditTrail logPinReset(String entityId, String entityName, Activity activity, String username, String instanceName, String narrative){
        AuditTrail auditTrail = null;
        EntityManager em = jpaApi.em();
        if (activity.isLogged()) {
            try {
                auditTrail = new AuditTrail();

                auditTrail.setAuditTrailId(GenerateKey.generateEntityId());
                auditTrail.setUsername(username.toUpperCase());
                auditTrail.setActivity(activity);
                auditTrail.setTime(LocalDateTime.now());
                auditTrail.setNarrative(narrative.toUpperCase());
                auditTrail.setInstanceName(instanceName);
                auditTrail.setEntityId(entityId);
                auditTrail.setEntityName(entityName);
                log.info(" ");
                em.persist(auditTrail);
                return auditTrail;
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return auditTrail;
    }
}
