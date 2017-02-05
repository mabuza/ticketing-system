package system.ticketing.profile.impl;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.JPAApi;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;
import system.ticketing.profile.api.AuditService;
import system.ticketing.profile.api.ProfileService;
import system.ticketing.profile.api.RoleService;
import system.ticketing.profile.dto.LogDTO;
import system.ticketing.profile.dto.PasswordDTO;
import system.ticketing.profile.dto.ProfileDTO;
import system.ticketing.profile.model.Profile;
import system.ticketing.profile.model.Role;
import system.ticketing.profile.util.AuditEvents;
import system.ticketing.profile.util.LdapUtil;
import system.ticketing.profile.util.PasswordUtil;
import system.ticketing.utilities.enums.EntityStatus;
import system.ticketing.utilities.util.GenerateKey;
import system.ticketing.utilities.util.SystemConstants;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static system.ticketing.profile.mapper.DTOMapper.toProfileDTO;
import static system.ticketing.profile.mapper.DTOMapper.toProfileEntity;

/**
 * Created by Ncube on 2/2/17.
 */
@SuppressWarnings("ALL")
public class ProfileServiceImpl implements ProfileService {
    private static final Logger log = LoggerFactory.getLogger(ProfileServiceImpl.class);

    private JPAApi jpaApi;
    private RoleService roleService;
    private AuditService auditService;
    private MailerClient mailerClient;

    @Inject
    public ProfileServiceImpl(JPAApi jpaApi, RoleService roleService, AuditService auditService,
                              MailerClient mailerClient){
        this.jpaApi = jpaApi;
        this.roleService = roleService;
        this.auditService = auditService;
        this.mailerClient = mailerClient;
    }

    @Override
    public ServiceCall<PasswordDTO, String> authenticateUser(){
        return (request) -> {
            log.info("In authenticate method");
            Profile profile = findProfileByUserNameAndStatus(request.getUserName(), EntityStatus.ACTIVE.name());
            log.info("------------------->" + profile.getFirstNames());
            if(profile == null){
                System.out.println("result is " + SystemConstants.AUTH_STATUS_INVALID_CREDENTIALS);
                return completedFuture(SystemConstants.AUTH_STATUS_INVALID_CREDENTIALS);
            }
            if(profile.getPasswordExpiryDate()!= null){
                if(LocalDateTime.now().isAfter(profile.getPasswordExpiryDate())){
                    profile.setStatus(EntityStatus.DISABLED);
                    jpaApi.withTransaction(() ->{
                        jpaApi.em().merge(profile);
                    });
                    return completedFuture(SystemConstants.AUTH_STATUS_PROFILE_EXPIRED);
                }
            }
            String result = LdapUtil.validateUser(request.getUserName(), request.getOldPassword());
            if(result.equals(SystemConstants.AUTH_STATUS_AUTHENTICATED)){
                if(profile.isChangePassword()){
                    result = SystemConstants.AUTH_STATUS_CHANGE_PASSWORD;
                    return completedFuture(result);
                }
                //profile.setLastLoginDate(LocalDateTime.now());
                profile.setLoggedIn(true);
                jpaApi.withTransaction(() -> {
                    jpaApi.em().merge(profile);
                });
            }
            return completedFuture(result);
        };
    }

    @Override
    public ServiceCall<NotUsed, String> logoutUser(String username) {
        return (request) -> {
            Profile profile = findProfileByUserName(username);
            profile.setLastLoginDate(LocalDateTime.now());
            profile.setLoggedIn(false);
            jpaApi.withTransaction(() -> {
                jpaApi.em().merge(profile);
            });
            return completedFuture("success");
        };

    }

    @Override
    public ServiceCall<PasswordDTO, String> checkUserPassword(){
        return (request) -> {
            String result = LdapUtil.validateUser(request.getUserName(), request.getOldPassword());
            return completedFuture(result);
        };
    }

    public String checkPassword(String userName, String password){
        return LdapUtil.validateUser(userName, password);
    }

    @Override
    public ServiceCall<NotUsed, String> test() {
        return (request) -> completedFuture("ProfileService is Up!!");
    }

    @Override
    @SuppressWarnings("unchecked")
    public ServiceCall<NotUsed, List<ProfileDTO>> getProfileByLastName(String lastName) {
        return (request) -> {
            List<Profile> results = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getProfileByLastName");
                query.setParameter("lastName", "%" + lastName.toUpperCase() + "%");
                query.setParameter("status", EntityStatus.DELETED);
                return (List<Profile>) query.getResultList();
            });
            return completedFuture(
                    Collections.unmodifiableList(
                            results.parallelStream().map(profile -> toProfileDTO(profile))
                                    .collect(Collectors.toList())));
        };
    }

    @Override
    public ServiceCall<NotUsed, List<ProfileDTO>> getProfileByStatus(String status) {
        return (request) -> {
            List<Profile> results = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getProfileByStatus");
                query.setParameter("status", status);
                return (List<Profile>) query.getResultList();
            });
            return completedFuture(
                    Collections.unmodifiableList(
                            results.parallelStream().map(profile -> toProfileDTO(profile))
                                    .collect(Collectors.toList())));
        };
    }

    @Override
    public ServiceCall<ProfileDTO, ProfileDTO> createProfile(String username) {
        return (request) -> {
            Profile entity = toProfileEntity(request);
            log.info("Profile -> " + entity.getId() + "|" + entity.getEntityName());
            entity.setStatus(EntityStatus.PENDING_APPROVAL);
            entity.setChangePassword(true);
            log.info("persisting profile");
            jpaApi.withTransaction(() -> {
                entity.setRole(jpaApi.em().find(Role.class, request.getRoleId()));
                jpaApi.em().persist(entity);
            });
            log.info(" done persisting profile");
            try {
                auditService.logActivity().invoke(LogDTO.builder()
                        .userName(username).activityName(AuditEvents.CREATE_PROFILE)
                        .entityId(entity.getId()).entityName(entity.getEntityName())
                        .newObject(entity.getAuditableAttributesString()).instanceName(entity.getInstanceName()).build());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Failed to log activity : " + e.getMessage());
            }
            return completedFuture(toProfileDTO(entity));
        };
    }



    @Override
    public ServiceCall<NotUsed, NotUsed> deleteProfile(String id, String username) {
        return (request) -> {
            jpaApi.withTransaction(() -> {
                Profile profile = jpaApi.em().find(Profile.class, id);
                profile.setStatus(EntityStatus.DELETED);
                jpaApi.em().merge(profile);
                LdapUtil.resetUserPassword(profile.getUserName(), PasswordUtil.getPassword(8));
                try {
                    auditService.logActivity().invoke(LogDTO.builder()
                            .userName(username).activityName(AuditEvents.DELETE_PROFILE)
                            .entityId(profile.getId()).entityName(profile.getEntityName())
                            .newObject(profile.getAuditableAttributesString()).instanceName(profile.getInstanceName()).build());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Failed to log activity : " + e.getMessage());
                }
            });
            return completedFuture(NotUsed.getInstance());
        };
    }

    @Override
    public ServiceCall<ProfileDTO, ProfileDTO> editProfile(String username) {
        return (request) -> {
            jpaApi.withTransaction(() -> {
                Profile oldEntity = jpaApi.em().find(Profile.class, request.getId());
                Profile newEntity = toProfileEntity(request);
                newEntity.setRole(jpaApi.em().find(Role.class, request.getRoleId()));
                newEntity.setVersion(oldEntity.getVersion());
                newEntity.setStatus(EntityStatus.PENDING_EDIT_APPROVAL);
                jpaApi.em().merge(newEntity);
                try {
                    auditService.logActivity().invoke(LogDTO.builder()
                            .userName(username).activityName(AuditEvents.EDIT_PROFILE)
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
    public ServiceCall<ProfileDTO, ProfileDTO> approveProfile(String username) {
        return (request) -> {
            Profile entity = jpaApi.withTransaction(() -> {
                Profile newEntity = jpaApi.em().find(Profile.class, request.getId());
                log.info("" + newEntity);
                String password = "";
                try {
                    password = PasswordUtil.getPassword(8);
                    newEntity.setUserPassword(password);
                    LdapUtil.createLDAPEntry(newEntity);
                    sendEmail(newEntity);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.debug("User Already In Directory");
                }
                newEntity.setStatus(EntityStatus.ACTIVE);
                newEntity.setUserPassword("");
                jpaApi.em().merge(newEntity);
                newEntity.setUserPassword(password);
                return newEntity;
            });
            try {
                auditService.logActivity().invoke(LogDTO.builder()
                        .userName(username).activityName(AuditEvents.APPROVE_PROFILE)
                        .entityId(entity.getId()).entityName(entity.getEntityName())
                        .newObject(entity.getAuditableAttributesString()).instanceName(entity.getInstanceName()).build());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Failed to log activity : " + e.getMessage());
            }
            return completedFuture(toProfileDTO(entity));
        };
    }

    @Override
    public ServiceCall<ProfileDTO, ProfileDTO> rejectProfile(String username) {
        return (request) -> {
            Profile entity = jpaApi.withTransaction(() -> {
                Profile newEntity = jpaApi.em().find(Profile.class, request.getId());newEntity.setStatus(EntityStatus.DISAPPROVED);
                newEntity = jpaApi.em().merge(newEntity);
                newEntity.setUserPassword(null);
                try {
                    auditService.logActivity().invoke(LogDTO.builder()
                            .userName(username).activityName(AuditEvents.REJECT_PROFILE)
                            .entityId(newEntity.getId()).entityName(newEntity.getEntityName())
                            .newObject(newEntity.getAuditableAttributesString()).instanceName(newEntity.getInstanceName()).build());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Failed to log activity : " + e.getMessage());
                }
                return newEntity;
            });
            return completedFuture(toProfileDTO(entity));
        };
    }

    @Override
    public ServiceCall<PasswordDTO, String> changeProfilePassword() {
        return (request) -> {
            String response = jpaApi.withTransaction(() -> {
                String result = "";
                String authResult = checkPassword(request.getUserName(), request.getOldPassword());
                Profile profile = findProfileByUserName(request.getUserName());
                //Profile profile = jpaApi.em().find(Profile.class, request.getProfileId());
                if(SystemConstants.AUTH_STATUS_AUTHENTICATED.equalsIgnoreCase(authResult)){
                    String oldProfile = this.findProfileById(profile.getId()).toString();
                    //profile.setUserPassword(request.getNewPassword());
                    jpaApi.em().merge(profile);
                    result = LdapUtil.changeUserPassword(profile.getUserName(), request.getOldPassword(),
                            request.getNewPassword());
                    if(result.equals(SystemConstants.CHANGE_PASSWORD_SUCCESS)){
                        profile.setChangePassword(false);
                        jpaApi.em().merge(profile);
                    }
                }else {
                    if (SystemConstants.AUTH_STATUS_INVALID_CREDENTIALS.equalsIgnoreCase(authResult)) {
                        return SystemConstants.INVALID_OLD_PASSWORD;
                    }
                    return authResult;
                }
                return result;
            });
            return completedFuture(response);
        };
    }

    @Override
    public ServiceCall<NotUsed, String> resetProfilePassword(String id) {
        return (request) -> {
            String response = jpaApi.withTransaction(() -> {
                Profile profile = jpaApi.em().find(Profile.class, id);
                String password = PasswordUtil.getPassword(8);
                String result = LdapUtil.resetUserPassword(profile.getUserName(), password);
                if(result.equals(SystemConstants.RESET_PASSWORD_SUCCESS)){
                    profile.setChangePassword(true);
                    profile.setUserPassword(password);
                    profile = jpaApi.em().merge(profile);
                    sendEmail(profile);
                }
                return result;
            });
            return completedFuture(response);
        };
    }

    @Override
    public ServiceCall<ProfileDTO, ProfileDTO> resetProfileIP(String id) {
        return (request) -> {
            jpaApi.withTransaction(() -> {
                Profile oldEntity = jpaApi.em().find(Profile.class, id);
                Profile newEntity = toProfileEntity(request);
                newEntity.setIpAddress("");
                newEntity = jpaApi.em().merge(newEntity);
            });
            return completedFuture(request);
        };
    }

    @Override
    public ServiceCall<NotUsed, List<ProfileDTO>> searchProfile(String search) {
        return (request) -> {
            List<Profile> results = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Session session = em.unwrap(Session.class);
                FullTextSession fullTextSession = Search.getFullTextSession(session);
                /*try {
                    fullTextSession.createIndexer().startAndWait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                QueryBuilder queryBuilder = fullTextSession.getSearchFactory()
                        .buildQueryBuilder().forEntity(Profile.class).get();
                org.apache.lucene.search.Query query = queryBuilder
                        .keyword()
                        .onFields("lastName", "firstNames", "userName", "ownerId", "email", "phoneNumber",
                                "mobileNumber", "role.name")
                        .matching(search)
                        .createQuery();
                org.hibernate.Query hibQuery = fullTextSession.createFullTextQuery(query, Profile.class);
                List<Profile> result = hibQuery.list();
                return result;
            });
            return completedFuture(
                    Collections.unmodifiableList(
                            results.parallelStream().map(profile -> toProfileDTO(profile))
                                    .collect(Collectors.toList())));
        };
    }

    @Override
    public ServiceCall<NotUsed, NotUsed> initSuperAdminProfile() {
        return null;
    }

    @Override
    public ServiceCall<ProfileDTO, String> sendOneTimePin() {
        return (request) -> {
            String otp = GenerateKey.generateSecurityCode();
            Profile profile = toProfileEntity(request);
            sendOneTimePinEmail(profile, otp);
            return completedFuture(otp);
        };
    }

    @Override
    public ServiceCall<NotUsed, Optional<ProfileDTO>> findProfileById(String id) {
        return (request) -> {
            Profile profile = jpaApi.withTransaction(() -> {
                Session session = (Session) jpaApi.em().getDelegate();
                return session.get(Profile.class, id);
            });
            if(profile != null){
                return completedFuture(Optional.ofNullable(toProfileDTO(profile)));
            }
            return completedFuture(Optional.empty());
        };
    }

    @Override
    public ServiceCall<NotUsed, ProfileDTO> getProfileByUserName(String userName) {
        return (request) -> {
            Profile profile = findProfileByUserName(userName);
            return completedFuture(toProfileDTO(profile));
        };
    }

    private Profile findProfileByUserName(String userName){
        Profile profile = jpaApi.withTransaction(() -> {
            Session session = (Session) jpaApi.em().getDelegate();
            org.hibernate.Query query = session.getNamedQuery("getProfileByUserName");
            query.setCacheable(true);
            query.setParameter("userName", userName);
            query.setParameter("status", EntityStatus.DELETED);
            return (Profile) query.uniqueResult();
        });
        return profile;
    }

    @Override
    public ServiceCall<NotUsed, ProfileDTO> getProfileByUserNameAndStatus(String userName, String status) {
        return (request) -> {
            Profile profile = jpaApi.withTransaction(() -> {
                return findProfileByUserNameAndStatus(userName, status);
            });
            return completedFuture(toProfileDTO(profile));
        };
    }

    private Profile findProfileByUserNameAndStatus(String userName, String status){
        Profile profile = jpaApi.withTransaction(() -> {
            EntityStatus profileStatus = EntityStatus.valueOf(status);
            EntityManager em = jpaApi.em();
            Query query = em.createNamedQuery("getProfileByUserNameAndStatus");
            query.setParameter("userName", userName);
            query.setParameter("status", profileStatus);
            return (Profile) query.getSingleResult();
        });
        return profile;
    }

    @Override
    public ServiceCall<NotUsed, List<ProfileDTO>> getProfileByUserRole(String role) {
        return (request) -> {
            List<Profile> results = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getProfileByUserRole");
                query.setParameter("role", "%"+role.toUpperCase()+"%");
                query.setParameter("status", EntityStatus.DELETED);
                return (List<Profile>) query.getResultList();
            });
            return completedFuture(
                    Collections.unmodifiableList(
                            results.parallelStream().map(profile -> toProfileDTO(profile))
                                    .collect(Collectors.toList())));
        };
    }

    @Override
    public ServiceCall<NotUsed, List<ProfileDTO>> getAllProfiles() {
        return (request) -> {
            List<Profile> results = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getAllProfiles");
                query.setMaxResults(100);
                query.setParameter("status", EntityStatus.DELETED);
                return (List<Profile>) query.getResultList();
            });
            log.info("List of profiles: "+results);
            return completedFuture(
                    Collections.unmodifiableList(
                            results.parallelStream().map(profile -> toProfileDTO(profile))
                                    .collect(Collectors.toList())));
        };
    }

    @Override
    public ServiceCall<NotUsed, List<ProfileDTO>> getProfileByBranchId(String branchId) {
        return (request) -> {
            List<Profile> results = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getProfileByBranchId");
                query.setParameter("branchId", branchId);
                query.setParameter("status", EntityStatus.DELETED);
                return (List<Profile>) query.getResultList();
            });
            return completedFuture(
                    Collections.unmodifiableList(
                            results.parallelStream().map(profile -> toProfileDTO(profile))
                                    .collect(Collectors.toList())));
        };
    }

    @Override
    public ServiceCall<NotUsed, List<ProfileDTO>> getAllLoggedOnUsers() {
        return (request) -> {
            List<Profile> results = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getAllLoggedOnProfiles");
                query.setParameter("loggedin",1);
                return (List<Profile>) query.getResultList();
            });
            return completedFuture(
                    Collections.unmodifiableList(
                            results.parallelStream().map(profile -> toProfileDTO(profile))
                                    .collect(Collectors.toList())));
        };
    }

    @Override
    public ServiceCall<NotUsed, ProfileDTO> getProfileByIP(String ipAdd) {
        return (request) -> {
            Profile profile = jpaApi.withTransaction(() -> {
                EntityManager em = jpaApi.em();
                Query query = em.createNamedQuery("getProfileByIP");
                query.setParameter("ipAddress",ipAdd);
                return (Profile) query.getSingleResult();
            });
            return completedFuture(toProfileDTO(profile));
        };
    }

    public void sendEmail(Profile profile) {
        try {
            log.info("Sending email async..");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy HH:mm:ss a");
            Email email = new Email()
                    .setSubject("SmartVend PPUS Login Details")
                    .setFrom("<ppusesolutions@gmail.com>")
                    .addTo("<" + profile.getEmail() + ">")
                    .setBodyText("The SmartVend Team")
                    .setBodyHtml("<html>\n" +
                            "    <head>\n" +
                            "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">             \n" +
                            "    </head>\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "    <body>\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "        <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"e4e4e4\"><tr><td>\n" +
                            "            <table id=\"top-message\" cellpadding=\"20\" cellspacing=\"0\" width=\"600\" align=\"center\">\n" +
                            "                <tr>\n" +
                            "                    <td align=\"left\">\n" +
                            "                        <h1>SmartVend</h1>\n" +
                            "                    </td>\n" +
                            "                </tr>\n" +
                            "            </table><!-- top message -->\n" +
                            "\n" +
                            "            <table id=\"header\" cellpadding=\"10\" cellspacing=\"0\" align=\"center\" bgcolor=\"1a1aff\">\n" +
                            "                <tr>\n" +
                            "                    <td width=\"570\" bgcolor=\"1a1aff\" style=\"color:#ffffff\"><h2>Your new password</h2></td>\n" +
                            "                </tr>\n" +
                            "                <tr>\n" +
                            "                    <td width=\"570\" align=\"right\" bgcolor=\"1a1aff\" style=\"color:#ffffff\"><p>"+formatter.format(LocalDateTime.now())+"</p></td>\n" +
                            "                </tr>\n" +
                            "            </table><!-- header -->\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "            <table id=\"main\" width=\"600\" align=\"center\" cellpadding=\"0\" cellspacing=\"15\">\n" +
                            "\n" +
                            "\n" +
                            "                <tr>\n" +
                            "                    <td>\n" +
                            "                        Hi "+profile.getFirstNames()+",   \n" +
                            "                    </td>\n" +
                            "                </tr><!-- content-5 -->\n" +
                            "                <tr>\n" +
                            "                    <td >\n" +
                            "                        Your profile on the SmartVend has been approved. You will be required to change your password on initial login.\n" +
                            "                    </td>\n" +
                            "                </tr>\n" +
                            "                <br />\n" +
                            "                <tr>\n" +
                            "                    <td>\n" +
                            "\n" +
                            "                        <table cellpadding=\"0\" cellspacing=\"0\" align=\"center\">\n" +
                            "                            <p align=\"center\">Your username is:  <strong>"+profile.getUserName()+" </strong><br />\n" +
                            "                                and <br />\n" +
                            "                                your password is: <strong>" + profile.getUserPassword()+"</strong></p>\n" +
                            "\n" +
                            "                        </table>                    \n" +
                            "                    </td>\n" +
                            "                </tr>\n" +
                            "                <tr>\n" +
                            "                    <td>\n" +
                            "                        <p></p><p></p>\n" +
                            "                        <table id=\"content-6\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\">\n" +
                            "                            <p align=\"center\">Please Make sure you enter a secure password. </p>\n" +
                            "                            <p align=\"center\" style=\"color:#4A72AF\">FOR ANY QUERIES. CONTACT OUR OFFICES ON (04) 480 344</p>\n" +
                            "                        </table>\n" +
                            "                    </td>\n" +
                            "                </tr>\n" +
                            "\n" +
                            "            </table><!-- main -->\n" +
                            "            <table id=\"bottom-message\" cellpadding=\"20\" cellspacing=\"0\" width=\"600\" align=\"center\">\n" +
                            "                <tr>\n" +
                            "                    <td align=\"center\">\n" +
                            "                        <p style=\"font-family: Arial, Helvetica, sans-serif\">You are receiving this email because you have a profile on SmartVend</p>\n" +
                            "                    </td>\n" +
                            "                </tr>\n" +
                            "            </table><!-- top message -->\n" +
                            "            </td></tr></table><!-- wrapper -->\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "    </body>\n" +
                            "</html>");

            mailerClient.send(email);
        } catch (Exception e) {
            log.error("Failed to send email : {}", e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendOneTimePinEmail(Profile profile, String otp) {
        try {
            log.info("Sending otp email async..");
            String cid = "1234";
            Email email = new Email()
                    .setSubject("SmartVend PPUS One Time PIN")
                    .setFrom("<ppusesolutions@gmail.com>")
                    .addTo("<" + profile.getEmail() + ">")
                    .setBodyText("Otp password")
                    .setBodyHtml("<html>\n" +
                            "    <head>\n" +
                            "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
                            "\n" +
                            "    </head>\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "    <body>\n" +
                            "        <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"e4e4e4\"><tr><td>\n" +
                            "            <table id=\"top-message\" cellpadding=\"20\" cellspacing=\"0\" width=\"600\" align=\"center\">\n" +
                            "                <tr>\n" +
                            "                    <td align=\"left\">\n" +
                            "                        <h1>SmartVend</h1>\n" +
                            "                    </td>\n" +
                            "                </tr>\n" +
                            "            </table><!-- top message -->\n" +
                            "\n" +
                            "\n" +
                            "            <table id=\"main\" width=\"600\" align=\"center\" cellpadding=\"0\" cellspacing=\"15\">\n" +
                            "                <tr>\n" +
                            "                    <td>\n" +
                            "                        Hi "+profile.getFirstNames()+",   \n" +
                            "                    </td>\n" +
                            "                </tr><!-- content-5 -->\n" +
                            "\n" +
                            "                <br />\n" +
                            "                <tr>\n" +
                            "                    <td>\n" +
                            "                        <p align=\"left\">Your SmartVend One Time PIN(OTP) for logging in is <strong>"+otp+"</strong>. This PIN will only work once </p>                   \n" +
                            "                    </td>\n" +
                            "                </tr>\n" +
                            "                <tr>\n" +
                            "                    <td>\n" +
                            "                        <p></p><p></p>\n" +
                            "                        <table id=\"content-6\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\">\n" +
                            "                            <p align=\"center\" style=\"color:#4A72AF\">FOR ANY QUERIES. CONTACT OUR OFFICES ON (04) 480 344</p>\n" +
                            "                        </table>\n" +
                            "                    </td>\n" +
                            "                </tr>\n" +
                            "            </table><!-- main -->\n" +
                            "            <table id=\"bottom-message\" cellpadding=\"20\" cellspacing=\"0\" width=\"600\" align=\"center\">\n" +
                            "                <tr>\n" +
                            "                    <td align=\"center\">\n" +
                            "                        <p style=\"font-family: Arial, Helvetica, sans-serif\">You are receiving this email because you have a profile on SmartVend</p>\n" +
                            "                    </td>\n" +
                            "                </tr>\n" +
                            "            </table><!-- top message -->\n" +
                            "            </td></tr></table><!-- wrapper -->\n" +
                            "\n" +
                            "\n" +
                            "    </body>\n" +
                            "</html>");
            mailerClient.send(email);
        } catch (Exception e) {
            log.error("Failed to send OTP email : {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
