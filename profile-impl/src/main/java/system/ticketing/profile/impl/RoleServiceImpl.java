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
import system.ticketing.profile.api.RoleService;
import system.ticketing.profile.dto.AccessRightDTO;
import system.ticketing.profile.dto.LogDTO;
import system.ticketing.profile.dto.RoleAccessRightDTO;
import system.ticketing.profile.dto.RoleDTO;
import system.ticketing.profile.model.AccessRight;
import system.ticketing.profile.model.Role;
import system.ticketing.profile.model.RoleAccessRight;
import system.ticketing.profile.util.AuditEvents;
import system.ticketing.utilities.enums.AccessRightStatus;
import system.ticketing.utilities.enums.AccessRights;
import system.ticketing.utilities.enums.EntityStatus;
import system.ticketing.utilities.util.GenerateKey;
import system.ticketing.utilities.util.Utils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static system.ticketing.profile.mapper.DTOMapper.*;

/**
 * Created by Ncube on 2/5/17.
 */
@SuppressWarnings("ALL")
public class RoleServiceImpl implements RoleService {
    static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);
    private JPAApi jpaApi;
    AuditService auditService;

    @Inject
    public RoleServiceImpl(JPAApi jpaApi, AuditService auditService){
        this.jpaApi = jpaApi;
        this.auditService = auditService;
    }

    @Override
    public ServiceCall<NotUsed, String> test() {
        return (request) -> completedFuture("RoleService is Up!!");
    }

    @Override
    public ServiceCall<RoleDTO, RoleDTO> createRole(String username) {
        return (request) -> {
            Role entity = toRoleEntity(request);
            log.info("Role -> " + entity.getId() + "|" + entity.getEntityName());
            entity.setStatus(EntityStatus.PENDING_APPROVAL);
            Role createdRole = jpaApi.withTransaction(() -> {
                jpaApi.em().persist(entity);
                try {
                    auditService.logActivity().invoke(LogDTO.builder()
                            .userName(username).activityName(AuditEvents.CREATE_ROLE)
                            .entityId(entity.getId()).entityName(entity.getEntityName())
                            .newObject(entity.getAuditableAttributesString()).instanceName(entity.getInstanceName()).build());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Failed to log activity : " + e.getMessage());
                }
                return entity;
            });
            try {
                createRoleAccessRights(createdRole);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return completedFuture(toRoleDTO(createdRole));
        };
    }

    public Role findRoleById(String id) {
        Role role = jpaApi.withTransaction(() -> {
            return jpaApi.em().find(Role.class, id);
        });
        return role;
    }

    public AccessRight getAccessRightById(String id){
        AccessRight accessRight = jpaApi.withTransaction(() ->{
            return jpaApi.em().find(AccessRight.class, id);
        });
        return accessRight;
    }

    @Override
    public ServiceCall<RoleDTO, RoleDTO> editRole(String username) {
        return (request) -> {
            jpaApi.withTransaction(() -> {
                Role oldEntity = jpaApi.em().find(Role.class, request.getId());
                Role newEntity = toRoleEntity(request);
                newEntity.setVersion(oldEntity.getVersion());
                jpaApi.em().merge(newEntity);
                try {
                    auditService.logActivity().invoke(LogDTO.builder()
                            .userName(username).activityName(AuditEvents.EDIT_ROLE)
                            .entityId(newEntity.getId()).entityName(newEntity.getEntityName())
                            .newObject(newEntity.getAuditableAttributesString()).instanceName(newEntity.getInstanceName()).build());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Failed to log activity : " + e.getMessage());
                }
            });
            return completedFuture(request);
        };
    }

    @Override
    public ServiceCall<RoleDTO, RoleDTO> approveRole(String username) {
        return (request) -> {
            jpaApi.withTransaction(() -> {
                Role role = jpaApi.em().find(Role.class, request.getId());
                role.setStatus(EntityStatus.ACTIVE);
                jpaApi.em().merge(role);
                try {
                    auditService.logActivity().invoke(LogDTO.builder()
                            .userName(username).activityName(AuditEvents.APPROVE_ROLE)
                            .entityId(role.getId()).entityName(role.getEntityName())
                            .newObject(role.getAuditableAttributesString()).instanceName(role.getInstanceName()).build());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Failed to log activity : " + e.getMessage());
                }
            });
            return completedFuture(request);
        };
    }

    @Override
    public ServiceCall<RoleDTO, RoleDTO> rejectRole(String username) {
        return (request) -> {
            jpaApi.withTransaction(() -> {
                Role role = jpaApi.em().find(Role.class, request.getId());
                role.setStatus(EntityStatus.DISAPPROVED);
                jpaApi.em().merge(role);
                try {
                    auditService.logActivity().invoke(LogDTO.builder()
                            .userName(username).activityName(AuditEvents.REJECT_ROLE)
                            .entityId(role.getId()).entityName(role.getEntityName())
                            .newObject(role.getAuditableAttributesString()).instanceName(role.getInstanceName()).build());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Failed to log activity : " + e.getMessage());
                }
            });
            return completedFuture(request);
        };
    }

    @Override
    public ServiceCall<NotUsed, NotUsed> deleteRole(String id, String username) {
        return (request) -> {
            jpaApi.withTransaction(() -> {

                Role role = jpaApi.em().find(Role.class, id);
                log.info("" + role.getProfiles());
                if(!(role.getProfiles()==null||role.getProfiles().isEmpty())){
                    try {
                        throw new Exception("Role cannot be deleted because it has profiles");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    role.setStatus(EntityStatus.DELETED);
                    jpaApi.em().merge(role);
                }
                try {
                    auditService.logActivity().invoke(LogDTO.builder()
                            .userName(username).activityName(AuditEvents.CREATE_PROFILE)
                            .entityId(role.getId()).entityName(role.getEntityName())
                            .newObject(role.getAuditableAttributesString()).instanceName(role.getInstanceName()).build());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Failed to log activity : " + e.getMessage());
                }
            });
            return completedFuture(NotUsed.getInstance());
        };
    }

    @Override
    public ServiceCall<NotUsed, Optional<RoleDTO>> findRoleByRoleId(String id) {
        return (request) -> {
            Role role = jpaApi.withTransaction(() -> {
                Session session = (Session) jpaApi.em().getDelegate();
                return session.get(Role.class, id);
            });
            if(role != null){
                return completedFuture(Optional.ofNullable(toRoleDTO(role)));
            }
            return completedFuture(Optional.empty());
        };
    }

    @Override
    public ServiceCall<NotUsed, RoleDTO> getRoleByRoleName(String roleName) {
        return (request) -> {
            Role role = jpaApi.withTransaction(() -> {
                Session session = (Session) jpaApi.em().getDelegate();
                org.hibernate.Query query = session.getNamedQuery("getRoleByRoleName");
                query.setCacheable(true);
                query.setParameter("name", roleName);
                return (Role) query.uniqueResult();
            });
            return completedFuture(toRoleDTO(role));
        };
    }

    @Override
    public ServiceCall<NotUsed, List<RoleDTO>> getRoleByStatus(String status) {
        return (request) -> {
            List<Role> results = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getRoleByStatus");
                query.setParameter("status", status);
                return (List<Role>) query.getResultList();
            });
            return completedFuture(
                    Collections.unmodifiableList(
                            results.parallelStream().map(role -> toRoleDTO(role))
                                    .collect(Collectors.toList())));
        };
    }

    @Override
    public ServiceCall<NotUsed, List<RoleDTO>> getActiveRoles() {
        return (request) -> {
            List<Role> results = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getActiveRoles");
                query.setParameter("status", EntityStatus.DELETED);
                return (List<Role>) query.getResultList();
            });
            return completedFuture(
                    Collections.unmodifiableList(
                            results.parallelStream().map(role -> toRoleDTO(role))
                                    .collect(Collectors.toList())));
        };
    }

    @Override
    public ServiceCall<NotUsed, List<RoleDTO>> searchRole(String search) {
        return (request) -> {
            List<Role> results = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Session session = em.unwrap(Session.class);
                FullTextSession fullTextSession = Search.getFullTextSession(session);
                /*try {
                    fullTextSession.createIndexer().startAndWait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                QueryBuilder queryBuilder = fullTextSession.getSearchFactory()
                        .buildQueryBuilder().forEntity(Role.class).get();
                org.apache.lucene.search.Query query = queryBuilder
                        .keyword()
                        .onFields("name")
                        .matching(search)
                        .createQuery();
                org.hibernate.Query hibQuery = fullTextSession.createFullTextQuery(query, Role.class);
                List<Role> result = hibQuery.list();
                return result;
            });
            return completedFuture(
                    Collections.unmodifiableList(
                            results.parallelStream().map(profile -> toRoleDTO(profile))
                                    .collect(Collectors.toList())));
        };
    }

    @SuppressWarnings("unchecked")
    private List<Role> getAllRoles(){
        List<Role> roleList = jpaApi.withTransaction(() -> {
            EntityManager em = jpaApi.em();
            Query query = em.createNamedQuery("getAllRoles");
            return (List<Role>) query.getResultList();
        });
        return roleList;
    }

    @Override
    public ServiceCall<NotUsed, AccessRightDTO> getAccessRightByActionName(String actionName) {
        return (request) -> {
            AccessRight accessRight = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getAccessRightByActionName");
                query.setParameter("actionName", "%" + actionName.toUpperCase() + "%");
                return (AccessRight) query.getSingleResult();
            });
            return completedFuture(toAccessRightDTO(accessRight));
        };
    }

    @Override
    public ServiceCall<AccessRightDTO, AccessRightDTO> enableAccessRight() {
        return (request) -> {
            jpaApi.withTransaction(() -> {
                AccessRight accessRight = jpaApi.em().find(AccessRight.class, request.getId());
                accessRight.setStatus(AccessRightStatus.ENABLED);
                jpaApi.em().merge(accessRight);
            });
            return completedFuture(request);
        };
    }

    @Override
    public ServiceCall<AccessRightDTO, AccessRightDTO> rejectAccessRight() {
        return (request) -> {
            jpaApi.withTransaction(() -> {
                AccessRight accessRight = jpaApi.em().find(AccessRight.class, request.getId());
                accessRight.setStatus(AccessRightStatus.DISABLED);
                jpaApi.em().merge(accessRight);
            });
            return completedFuture(request);
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public ServiceCall<NotUsed, List<AccessRightDTO>> getAccessRight() {
        return (request) -> {
            List<AccessRight> results = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getAccessRight");
                query.setParameter("status", AccessRightStatus.DELETED);
                return (List<AccessRight>) query.getResultList();
            });
            return completedFuture(
                    Collections.unmodifiableList(
                            results.parallelStream().map(accessRight -> toAccessRightDTO(accessRight))
                                    .collect(Collectors.toList())));
        };
    }

    @Override
    public ServiceCall<AccessRightDTO, AccessRightDTO> createAccessRight() {
        return (request) -> {
            AccessRight entity = toAccessRightEntity(request);
            entity.setFieldsToUpperCase();
            log.info("AccessRight -> " + entity.getActionName());
            AccessRight finalEntity = entity;
            jpaApi.withTransaction(() -> {
                jpaApi.em().persist(finalEntity);
            });
            this.getAccessRightByActionName(entity.getActionName());
            try {
                refreshRoles(entity);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return completedFuture(toAccessRightDTO(entity));
        };

    }

    @Override
    public ServiceCall<NotUsed, NotUsed> deleteAccessRight(String id) {
        return (request) -> {
            jpaApi.withTransaction(() -> {
                AccessRight accessRight = jpaApi.em().find(AccessRight.class, id);
                accessRight.setStatus(AccessRightStatus.DELETED);
                jpaApi.em().merge(accessRight);
            });
            return completedFuture(NotUsed.getInstance());
        };
    }

    @Override
    public ServiceCall<AccessRightDTO, AccessRightDTO> editAccessRight() {
        return (request) -> {
            jpaApi.withTransaction(() -> {
                AccessRight oldEntity = jpaApi.em().find(AccessRight.class, request.getId());
                AccessRight newEntity = toAccessRightEntity(request);
                newEntity.setFieldsToUpperCase();
                newEntity.setVersion(oldEntity.getVersion());
                jpaApi.em().merge(newEntity);
            });
            return completedFuture(request);
        };
    }

    @Override
    public ServiceCall<NotUsed, AccessRightDTO> findAccessRightById(String id) {
        return (request) -> {
            AccessRight accessRight = jpaApi.withTransaction(() -> {
                return jpaApi.em().find(AccessRight.class, id);
            });
            return completedFuture(toAccessRightDTO(accessRight));
        };
    }

    @Override
    public ServiceCall<NotUsed, List<AccessRightDTO>> searchAccessRight(String actionName) {
        return (request) -> {
            List<AccessRight> results = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Session session = em.unwrap(Session.class);
                FullTextSession fullTextSession = Search.getFullTextSession(session);
                /*try {
                    fullTextSession.createIndexer().startAndWait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                QueryBuilder queryBuilder = fullTextSession.getSearchFactory()
                        .buildQueryBuilder().forEntity(AccessRight.class).get();
                org.apache.lucene.search.Query query = queryBuilder
                        .keyword()
                        .onFields("actionName")
                        .matching(actionName)
                        .createQuery();
                org.hibernate.Query hibQuery = fullTextSession.createFullTextQuery(query, AccessRight.class);
                List<AccessRight> result = hibQuery.list();
                return result;
            });
            return completedFuture(
                    Collections.unmodifiableList(
                            results.parallelStream().map(accessRight -> toAccessRightDTO(accessRight))
                                    .collect(Collectors.toList())));
        };
    }

    private void createRoleAccessRights(Role role) throws Exception {
        try {
            final CompletionStage<Object> superAdminInit = this.getAccessRight().invoke().thenApply(accessRightDTOList -> {
                if (accessRightDTOList.isEmpty()) {
                    log.info("AccessRights EMPY.. Initialize");
                    try {
                        accessRightDTOList = this.initializeAccessRights().get(30, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                final Set<AccessRightDTO> finalAccessRightDTOList = Collections.unmodifiableSet(
                        new HashSet<>(accessRightDTOList));
                jpaApi.withTransaction(() -> {
                    Role managedRole = jpaApi.em().find(Role.class, role.getId());
                    finalAccessRightDTOList.stream().forEach(accessRightDTO -> {
                        RoleAccessRight rar = new RoleAccessRight();
                        rar.setId(GenerateKey.generateEntityId());
                        rar.setAccessRight(jpaApi.em().find(AccessRight.class, accessRightDTO.getId()));
                        rar.setRole(managedRole);
                        rar.setDateCreated(LocalDateTime.now());
                        rar.setCanDo(Utils.isSuperAdminRole(role.getName()));
                        jpaApi.em().persist(rar);
                        log.info("Created RoleAccessRight -> {}|{}", role.getName(), accessRightDTO.getActionName());
                    });
                });
                return role;
            });
            log.info("Super Admin Init Done..");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private AccessRight refreshRoles(AccessRight accessRight) throws Exception{
        return jpaApi.withTransaction(() -> {
            EntityManager em = jpaApi.em();
            List<Role> roleList = getAllRoles();
            if(roleList!=null)
                for (Role role : roleList) {
                    RoleAccessRight rar = new RoleAccessRight();
                    rar.setId(GenerateKey.generateEntityId());
                    rar.setAccessRight(accessRight);
                    rar.setRole(role);
                    rar.setCanDo(Utils.isSuperAdminRole(role.getName()));
                    em.persist(rar);
                }
            //AccessRight value = em.merge(accessRight);
            return em.merge(toAccessRightEntity(toAccessRightDTO(accessRight)));
        });
    }

    @Override
    public ServiceCall<RoleAccessRightDTO, RoleAccessRightDTO> updateRoleAccessRight(String username) {
        return (request) -> {
            jpaApi.withTransaction(() -> {
                RoleAccessRight oldEntity = jpaApi.em().find(RoleAccessRight.class, request.getId());
                log.info("UPDATE RoleAccessRight ->"+ oldEntity.getRole().getName() + "|" + oldEntity.getAccessRight().getActionName());
                RoleAccessRight newEntity = toRoleAccessRightEntity(request);
                newEntity.setRole(oldEntity.getRole());
                newEntity.setAccessRight(oldEntity.getAccessRight());
                newEntity.setVersion(oldEntity.getVersion());
                jpaApi.em().merge(newEntity);
                try {
                    auditService.logActivity().invoke(LogDTO.builder()
                            .userName(username).activityName(AuditEvents.CREATE_PROFILE)
                            .entityId(newEntity.getId()).entityName(newEntity.getEntityName())
                            .newObject(newEntity.getAuditableAttributesString()).instanceName(newEntity.getInstanceName()).build());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Failed to log activity : " + e.getMessage());
                }
            });
            return completedFuture(request);
        };
    }

    @Override
    public ServiceCall<NotUsed, NotUsed> updateRoleAccessRightByIdAndCanDo(String id, String canDo) {
        return (request) -> {
            log.info("CanDo update params -> " + id + "|" + canDo);
            jpaApi.withTransaction(() -> {
                Query query = jpaApi.em().createQuery("UPDATE RoleAccessRight r SET r.canDo = :canDo WHERE r.id = :id");
                query.setParameter("canDo", Boolean.valueOf(canDo));
                query.setParameter("id", id);
                int result = query.executeUpdate();
                log.info("CanDo update result -> " + result);
                System.out.println("CanDo update result -> " + result);
            });
            return completedFuture(NotUsed.getInstance());
        };
    }

    @Override
    public ServiceCall<NotUsed, RoleAccessRightDTO> findRoleAccessRightById(String id) {
        return (request) -> {
            RoleAccessRight roleAccessRight = jpaApi.withTransaction(() -> {
                return jpaApi.em().find(RoleAccessRight.class, id);
            });
            return completedFuture(toRoleAccessRightDTO(roleAccessRight));
        };
    }

    @Override
    public ServiceCall<NotUsed, Optional<RoleAccessRightDTO>> getRoleAccessRightByRoleNameAndActionName(String roleName, String actionName) {
        return (request) -> {
            //log.info("----- roleName & actionName -> " + roleName + "|" + actionName);
            RoleAccessRight profile = jpaApi.withTransaction(() -> {
                Session session = (Session) jpaApi.em().getDelegate();
                org.hibernate.Query query = session.getNamedQuery("getRoleAccessRightByRoleNameAndActionName");
                query.setCacheable(true);
                query.setParameter("roleName", roleName);
                query.setParameter("actionName", actionName);
                return (RoleAccessRight) query.uniqueResult();
            });
            return completedFuture(Optional.ofNullable(toRoleAccessRightDTO(profile)));
        };
    }

    @Override
    public ServiceCall<NotUsed, List<RoleAccessRightDTO>> getRoleAccessRightByRole(String role) {
        return (request) -> {
            List<RoleAccessRight> results = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getRoleAccessRightByRole");
                query.setParameter("roleId", role);
                return (List<RoleAccessRight>) query.getResultList();
            });
            return completedFuture(
                    Collections.unmodifiableList(
                            results.parallelStream().map(roleAccessRight -> toRoleAccessRightDTO(roleAccessRight))
                                    .collect(Collectors.toList())));
        };
    }

    private CompletableFuture<List<AccessRightDTO>> initializeAccessRights() {
        try {
            final List<String> accessRights = Stream.of(AccessRights.values())
                    .map(AccessRights::name)
                    .collect(Collectors.toList());
            for (String actionName: accessRights) {
                AccessRight accessRight = new AccessRight();
                accessRight.setId(GenerateKey.generateEntityId());
                accessRight.setStatus(AccessRightStatus.ENABLED);
                accessRight.setActionName(actionName);
                accessRight.setDateCreated(LocalDateTime.now());
                this.initializeAccessRight(accessRight);
            }
            return completedFuture(this.getAccessRight().invoke().toCompletableFuture().get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return completedFuture(new ArrayList<AccessRightDTO>());
    }

    private void initializeAccessRight(AccessRight accessRight) {
        accessRight.setFieldsToUpperCase();
        AccessRight finalEntity = accessRight;
        jpaApi.withTransaction(() -> {
            jpaApi.em().persist(finalEntity);
        });
        log.info("Created AccessRight -> {}", accessRight.getActionName());
    }
}
