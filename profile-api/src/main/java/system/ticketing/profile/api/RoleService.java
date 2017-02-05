package system.ticketing.profile.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.CircuitBreaker;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import system.ticketing.profile.dto.AccessRightDTO;
import system.ticketing.profile.dto.RoleAccessRightDTO;
import system.ticketing.profile.dto.RoleDTO;

import java.util.List;
import java.util.Optional;

import static com.lightbend.lagom.javadsl.api.Service.*;

/**
 * Created by Ncube on 2/5/17.
 */
public interface RoleService extends Service{
    ServiceCall<NotUsed, String> test();
    ServiceCall<RoleDTO, RoleDTO> createRole(String username);
    ServiceCall<RoleDTO, RoleDTO> editRole(String username);
    ServiceCall<RoleDTO, RoleDTO> approveRole(String username);
    ServiceCall<RoleDTO, RoleDTO> rejectRole(String username);
    ServiceCall<NotUsed, NotUsed> deleteRole(String id, String username);
    ServiceCall<NotUsed, Optional<RoleDTO>> findRoleByRoleId(String id);
    ServiceCall<NotUsed, RoleDTO> getRoleByRoleName(String name);
    ServiceCall<NotUsed, List<RoleDTO>> getRoleByStatus(String status);
    ServiceCall<NotUsed, List<RoleDTO>> getActiveRoles();
    ServiceCall<NotUsed, List<RoleDTO>> searchRole(String search);
    ServiceCall<NotUsed, AccessRightDTO> getAccessRightByActionName(String actionName);
    ServiceCall<AccessRightDTO, AccessRightDTO> enableAccessRight();
    ServiceCall<AccessRightDTO, AccessRightDTO> rejectAccessRight();
    ServiceCall<NotUsed, List<AccessRightDTO>> getAccessRight();
    ServiceCall<AccessRightDTO, AccessRightDTO> createAccessRight();
    ServiceCall<NotUsed, NotUsed> deleteAccessRight(String id);
    ServiceCall<AccessRightDTO, AccessRightDTO> editAccessRight();
    ServiceCall<NotUsed, AccessRightDTO> findAccessRightById(String id);
    ServiceCall<NotUsed, List<AccessRightDTO>> searchAccessRight(String search);
    ServiceCall<RoleAccessRightDTO, RoleAccessRightDTO> updateRoleAccessRight(String username);
    ServiceCall<NotUsed, NotUsed> updateRoleAccessRightByIdAndCanDo(String id, String canDo);
    ServiceCall<NotUsed, RoleAccessRightDTO> findRoleAccessRightById(String id);
    ServiceCall<NotUsed, Optional<RoleAccessRightDTO>> getRoleAccessRightByRoleNameAndActionName(String roleName, String actionName);
    ServiceCall<NotUsed, List<RoleAccessRightDTO>> getRoleAccessRightByRole(String id);

    @Override
    default Descriptor descriptor(){
        // @formatter:off
        return named("roleService").withCalls(
                restCall(Method.GET, "/api/role/test", this::test),
                restCall(Method.POST, "/api/roles/?username", this::createRole)
                        .withCircuitBreaker(CircuitBreaker.identifiedBy("longRunningProcessBreaker")),
                restCall(Method.PUT, "/api/roles/?username", this::editRole),
                restCall(Method.GET, "/api/roles/active", this::getActiveRoles),
                restCall(Method.PUT, "/api/roles/approve/?username", this::approveRole),
                restCall(Method.PUT, "/api/roles/reject/?username", this::rejectRole),
                restCall(Method.DELETE, "/api/roles/delete/?id&username", this::deleteRole),
                restCall(Method.GET, "/api/roles/:id", this::findRoleByRoleId)
                        .withCircuitBreaker(CircuitBreaker.identifiedBy("longRunningProcessBreaker")),
                restCall(Method.GET, "/api/roles/name/:role", this::getRoleByRoleName),
                restCall(Method.GET, "/api/roles/status/:status", this::getRoleByStatus),
                restCall(Method.GET, "/api/roles/search/:search", this::searchRole),
                restCall(Method.GET, "/api/accessright/test", this::test),
                restCall(Method.GET, "/api/accessrights", this::getAccessRight),
                restCall(Method.GET, "/api/accessrights/:actionName", this::getAccessRightByActionName),
                restCall(Method.PUT, "/api/accessrights/enable", this::enableAccessRight),
                restCall(Method.PUT, "/api/accessrights/reject", this::rejectAccessRight),
                restCall(Method.POST, "/api/accessrights", this::createAccessRight),
                restCall(Method.DELETE, "/api/accessrights/delete/:id", this::deleteAccessRight),
                restCall(Method.PUT, "/api/accessrights", this::editAccessRight),
                restCall(Method.GET, "/api/accessrights/id/:id", this::findAccessRightById),
                restCall(Method.GET, "/api/accessrights/search/:search", this::searchAccessRight),
                restCall(Method.GET, "/api/roleaccessrights/role/:id", this::getRoleAccessRightByRole),
                restCall(Method.GET, "/api/roleaccessright/test", this::test),
                restCall(Method.PUT, "/api/roleaccessrights/update/?username", this::updateRoleAccessRight),
                restCall(Method.GET, "/api/roleaccessrights/updateCanDo/?id&canDo", this::updateRoleAccessRightByIdAndCanDo),
                restCall(Method.GET,"/api/roleaccessrights/id/:id", this::findRoleAccessRightById),
                restCall(Method.GET, "/api/roleaccessrights/getByRoleNameAndActionName/?roleName&actionName", this::getRoleAccessRightByRoleNameAndActionName)
        ).withAutoAcl(true);
        // @formatter:on
    }
}
