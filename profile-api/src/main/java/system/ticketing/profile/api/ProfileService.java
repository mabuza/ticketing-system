package system.ticketing.profile.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.CircuitBreaker;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import system.ticketing.profile.dto.PasswordDTO;
import system.ticketing.profile.dto.ProfileDTO;

import java.util.List;
import java.util.Optional;

import static com.lightbend.lagom.javadsl.api.Service.*;

/**
 * Created by Ncube on 2/2/17.
 */
public interface ProfileService extends Service{
    ServiceCall<NotUsed, String> test();
    ServiceCall<NotUsed, List<ProfileDTO>> getProfileByLastName(String lastName);
    ServiceCall<NotUsed, List<ProfileDTO>> getProfileByStatus(String status);
    ServiceCall<ProfileDTO, ProfileDTO> createProfile(String username);
    ServiceCall<NotUsed, NotUsed> deleteProfile(String id, String username);
    ServiceCall<ProfileDTO, ProfileDTO> editProfile(String username);
    ServiceCall<ProfileDTO, ProfileDTO> approveProfile(String username);
    ServiceCall<PasswordDTO, String> authenticateUser();
    ServiceCall<NotUsed, String> logoutUser(String username);
    ServiceCall<PasswordDTO, String> checkUserPassword();
    ServiceCall<ProfileDTO, ProfileDTO> rejectProfile(String username);
    ServiceCall<PasswordDTO, String> changeProfilePassword();
    ServiceCall<NotUsed, String> resetProfilePassword(String id);
    ServiceCall<NotUsed, Optional<ProfileDTO>> findProfileById(String id);
    ServiceCall<NotUsed, ProfileDTO> getProfileByUserName(String userName);
    ServiceCall<NotUsed, ProfileDTO> getProfileByUserNameAndStatus(String userName, String status);
    ServiceCall<NotUsed, List<ProfileDTO>> getProfileByUserRole(String role);
    ServiceCall<NotUsed, List<ProfileDTO>> getAllProfiles();
    ServiceCall<NotUsed, List<ProfileDTO>> getProfileByBranchId(String branchId);
    ServiceCall<NotUsed, List<ProfileDTO>> getAllLoggedOnUsers();
    ServiceCall<NotUsed, ProfileDTO> getProfileByIP(String ipAdd);
    ServiceCall<ProfileDTO, ProfileDTO> resetProfileIP(String id);
    ServiceCall<NotUsed, List<ProfileDTO>> searchProfile(String search);
    ServiceCall<NotUsed, NotUsed> initSuperAdminProfile();
    ServiceCall<ProfileDTO, String> sendOneTimePin();

    @Override
    default Descriptor descriptor() {
        // @formatter:off
        return named("profileService").withCalls(
                restCall(Method.GET, "/api/profile/test", this::test),
                restCall(Method.GET, "/api/profiles/initAdminProfile", this::initSuperAdminProfile)
                        .withCircuitBreaker(CircuitBreaker.identifiedBy("longRunningProcessBreaker")),
                restCall(Method.GET, "/api/profiles/search/:search", this::searchProfile),
                restCall(Method.GET, "/api/profiles/:lastName", this::getProfileByLastName),
                restCall(Method.GET, "/api/profiles/:status", this::getProfileByStatus),
                restCall(Method.POST, "/api/profiles/?username", this::createProfile),
                restCall(Method.PUT, "/api/profiles/?username", this::editProfile),
                restCall(Method.DELETE, "/api/profiles/?id&username", this::deleteProfile),
                restCall(Method.PUT, "/api/profiles/approve/?username", this::approveProfile),
                restCall(Method.POST, "/api/profiles/authenticate", this::authenticateUser),
                restCall(Method.GET, "/api/profiles/logout/:username", this::logoutUser),
                restCall(Method.POST, "/api/profiles/validate", this::checkUserPassword),
                restCall(Method.PUT, "/api/profiles/reject/?username", this::rejectProfile),
                restCall(Method.PUT, "/api/profiles/change", this::changeProfilePassword),
                restCall(Method.PUT, "/api/profiles/:id", this::resetProfilePassword),
                restCall(Method.GET, "/api/profiles/id/:id", this::findProfileById),
                restCall(Method.GET, "/api/profiles/username/:userName", this::getProfileByUserName),
                restCall(Method.GET, "/api/profiles/:userName/:status", this::getProfileByUserNameAndStatus),
                restCall(Method.GET, "/api/profiles/:role", this::getProfileByUserRole),
                restCall(Method.GET, "/api/profiles", this::getAllProfiles),
                restCall(Method.GET, "/api/profiles/:branchId", this::getProfileByBranchId),
                restCall(Method.GET, "/api/profiles", this::getAllLoggedOnUsers),
                restCall(Method.GET, "/api/profiles/ip/:ipAddress", this::getProfileByIP),
                restCall(Method.PUT, "/api/profiles/:ipAddress", this::resetProfileIP),
                restCall(Method.POST, "/api/profiles/OTP", this::sendOneTimePin)
        ).withAutoAcl(true);
        // @formatter:on
    }
}
