package system.ticketing.profile.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Data;
import system.ticketing.utilities.enums.EntityStatus;
import system.ticketing.utilities.util.GenerateKey;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Created by Ncube on 2/5/17.
 */
@Data
public class ProfileDTO implements Jsonable{
    private String id;
    private String lastName;
    private String firstNames;
    private String userName;
    private String status;
    private String userPassword;
    private String email;
    private String phoneNumber;
    private String ownerType;
    private String ownerId;
    private String owner;
    private String mobileNumber;
    private LocalDateTime lastLoginDate;
    private int loginAttempts;
    private LocalDateTime passwordExpiryDate;
    private boolean changePassword;
    private boolean loggedIn;
    private String ipAddress;
    private String branchId;
    private String roleId;
    private String roleName;
    private LocalDateTime dateCreated;
    private RoleDTO role;
    public ProfileDTO(){}

    @JsonCreator
    public ProfileDTO(
            @JsonProperty("id") String id,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("firstNames") String firstNames,
            @JsonProperty("userName") String userName,
            @JsonProperty("status") String status,
            @JsonProperty("userPassword") String userPassword,
            @JsonProperty("email") String email,
            @JsonProperty("phoneNumber") String phoneNumber,
            @JsonProperty("ownerType") String ownerType,
            @JsonProperty("ownerId") String ownerId,
            @JsonProperty("owner") String owner,
            @JsonProperty("mobileNumber") String mobileNumber,
            @JsonProperty("lastLoginDate") LocalDateTime lastLoginDate,
            @JsonProperty("loginAttempts") int loginAttempts,
            @JsonProperty("passwordExpiryDate") LocalDateTime passwordExpiryDate,
            @JsonProperty("changePassword") boolean changePassword,
            @JsonProperty("loggedIn") boolean loggedIn,
            @JsonProperty("ipAddress") String ipAddress,
            @JsonProperty("branchId") String branchId,
            @JsonProperty("roleId") String roleId,
            @JsonProperty("roleName")String roleName,
            @JsonProperty("role") RoleDTO role,
            @JsonProperty("dateCreated") LocalDateTime dateCreated) {
        this.id = Optional.ofNullable(id).orElseGet(GenerateKey::generateEntityId);
        this.lastName = Preconditions.checkNotNull(lastName, "lastName");
        this.firstNames = Preconditions.checkNotNull(firstNames, "firstNames");
        this.userName = Preconditions.checkNotNull(userName, "userName");
        this.status = Optional.ofNullable(status).orElseGet(EntityStatus.PENDING_APPROVAL::name);
        this.userPassword = userPassword;
        this.email = Preconditions.checkNotNull(email, "email");
        this.phoneNumber = phoneNumber;
        this.ownerType = Preconditions.checkNotNull(ownerType, "ownerType");
        this.ownerId = Preconditions.checkNotNull(ownerId, "ownerId");
        this.owner = owner;
        this.mobileNumber = Preconditions.checkNotNull(mobileNumber, "mobileNumber");
        this.lastLoginDate = Optional.ofNullable(lastLoginDate).orElseGet(LocalDateTime::now);
        this.loginAttempts = loginAttempts;
        this.passwordExpiryDate = passwordExpiryDate;
        this.changePassword = changePassword;
        this.loggedIn = loggedIn;
        this.ipAddress = ipAddress;
        this.branchId = branchId;
        this.roleId = roleId;
        this.roleName = roleName;
        this.dateCreated = Optional.ofNullable(dateCreated).orElseGet(LocalDateTime::now);
        this.role = role;
    }
}
